package com.safeway.app.emju.mylist.service.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.google.inject.Inject;
import com.safeway.app.emju.allocation.dao.CCAllocationDAO;
import com.safeway.app.emju.allocation.model.CCAllocatedOffer;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.helper.ValidationHelper;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.Utils;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.libs.Akka;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

public class CCItemDetailAsyncRetriever extends AbstractItemDetailAsyncRetriever<OfferDetail> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CCItemDetailAsyncRetriever.class);
	
	private OfferDetailCache offerCache;
	private CCAllocationDAO ccAllocationDAO;
	
	@Inject
	public CCItemDetailAsyncRetriever(final OfferDetailCache offerCache, final CCAllocationDAO ccAllocationDAO) {
		
		this.ccAllocationDAO = ccAllocationDAO;
		this.offerCache = offerCache;
	}

	@Override
	public Promise<Map<String, OfferDetail>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException {
		
		return Promise.promise(() -> this.getOfferDetails(itemMap, shoppingListVO));
	}
	
	@Override
	public Promise<Map<String, OfferDetail>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, ExecutionContext threadCtx) throws ApplicationException {
		
		ExecutionContext exeCtx = threadCtx != null ? threadCtx : Akka.system().dispatchers().defaultGlobalDispatcher();
		
		Promise<Map<String, OfferDetail>> promise = F.Promise.promise((Function0<Map<String, OfferDetail>>) () -> {
            return this.getOfferDetails(itemMap, shoppingListVO);
        } , exeCtx);
		
		return promise;
	}
	
	protected Map<String, OfferDetail> getOfferDetails(Map<String, ShoppingListItem> itemMap, ShoppingListVO shoppingListVO)
			throws ApplicationException {
		
		LOGGER.info("CCDetailsProvider getOfferDetails >>");
		Map<String, OfferDetail> offerDetailMap = new HashMap<String, OfferDetail>();
		List<Long> validOfferIds = new ArrayList<Long>();
		List<Long> lookupOfferIds = new ArrayList<Long>();
		
		//String postalCode = shoppingListVO.getHeaderVO().getPreferredStore().getPostalCode();
		Integer storeId = new Integer(shoppingListVO.getHeaderVO().getParamStoreId());
		for (Entry<String, ShoppingListItem> entry : itemMap.entrySet()) {
			LOGGER.debug("lookupOfferIds ==> " + entry.getKey());
			lookupOfferIds.add(Long.parseLong(entry.getKey()));
		}
		
		LOGGER.info("CCDetailsProvider before finding ccAllocations>>" + lookupOfferIds.size());
		Map<Long, CCAllocatedOffer> allocatedOffersStoreOnly = ccAllocationDAO.findCCAllocation(storeId);
		
		Map<Long, CCAllocatedOffer> allocatedOffers = new HashMap<Long, CCAllocatedOffer>();
		allocatedOffers.putAll(allocatedOffersStoreOnly);
		
		LOGGER.info("CCDetailsProvider after finding ccAllocations>>" + allocatedOffers.size());
		
		for(Entry<Long, CCAllocatedOffer> entry : allocatedOffers.entrySet()) {
				
			if(lookupOfferIds.contains(entry.getKey())) {
				validOfferIds.add(entry.getKey());
			}
		}
			
			
		LOGGER.debug("Before getting offer details from cache");
		Long[] validOfferIdsArray = validOfferIds.toArray(new Long[validOfferIds.size()]);
		LOGGER.debug("Valid ids= " + validOfferIds);
		offerCache.getOfferDetailsByIds(validOfferIdsArray);
		offerDetailMap = convertKeysToString(offerCache.getOfferDetailsByIds(validOfferIdsArray));
		LOGGER.debug("OfferDetailMap size being returned: " + offerDetailMap.size());
		return offerDetailMap;
	}
	
	public Map<String, OfferDetail> convertKeysToString(Map<Long, OfferDetail> offerMap) {
		
		 Map<String, OfferDetail> transformedOfferMap = new HashMap<String, OfferDetail>();
		 
		 Set<Long> keySet = offerMap.keySet();
		 Iterator<Long> itr = keySet.iterator();
		 while(itr.hasNext()) {
			 Long key = itr.next();
			 
			 transformedOfferMap.put(key.toString(), offerMap.get(key));
			 
		 }
		 
		 return transformedOfferMap;		 
		 
		
	}

}
