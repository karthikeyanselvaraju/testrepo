package com.safeway.app.emju.mylist.service.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.libs.Akka;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

public class TRItemDetailAsyncRetriever extends AbstractItemDetailAsyncRetriever<OfferDetail> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TRItemDetailAsyncRetriever.class);
	
	private OfferDetailCache offerCache;
	
	@Inject
	public TRItemDetailAsyncRetriever(final OfferDetailCache offerCache) {
		this.offerCache = offerCache;
	}

	public Map<String, OfferDetail> getDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException {
		
		return getOfferDetails(itemMap, shoppingListVO);
	}
	
	@Override
	public Promise<Map<String, OfferDetail>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException {
		return Promise.promise(() -> getOfferDetails(itemMap, shoppingListVO));
	}
	
	@Override
	public Promise<Map<String, OfferDetail>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, ExecutionContext threadCtx) throws ApplicationException {
		
         ExecutionContext exeCtx = threadCtx != null ? threadCtx : Akka.system().dispatchers().defaultGlobalDispatcher();
		
		Promise<Map<String, OfferDetail>> promise = F.Promise.promise((Function0<Map<String, OfferDetail>>) () -> {
            return getOfferDetails(itemMap, shoppingListVO);
        } , exeCtx);
		
		return promise;		
	}
	
	private Map<String, OfferDetail> getOfferDetails(Map<String, ShoppingListItem> itemMap, ShoppingListVO shoppingListVO)
			throws ApplicationException {
		
		LOGGER.info("getOfferDetails >>");
		Map<String, OfferDetail> offerDetailMap = new HashMap<String, OfferDetail>();
		List<Long> validOfferIds = new ArrayList<Long>();
		
		for (Entry<String, ShoppingListItem> entry : itemMap.entrySet()) {
			LOGGER.debug("lookupOfferIds ==> " + entry.getKey());
			validOfferIds.add(Long.parseLong(entry.getKey()));
		}
				
		LOGGER.debug("Before getting offer details from cache");
		
		Long[] validOfferIdsArray = validOfferIds.toArray(new Long[validOfferIds.size()]);
		LOGGER.debug("Valid ids= " + validOfferIds);
		
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
