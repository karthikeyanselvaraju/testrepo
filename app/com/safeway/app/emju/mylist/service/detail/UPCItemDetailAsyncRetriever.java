package com.safeway.app.emju.mylist.service.detail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.safeway.app.emju.cache.RetailScanCache;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.RetailScanOffer;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.libs.Akka;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

public class UPCItemDetailAsyncRetriever extends AbstractItemDetailAsyncRetriever<RetailScanOffer> {
	
	private RetailScanCache retailScanCache;
	
	@Inject
	public UPCItemDetailAsyncRetriever(RetailScanCache retailScanCache) {
		
		this.retailScanCache = retailScanCache;
	}

	@Override
	public Promise<Map<String, RetailScanOffer>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Promise<Map<String, RetailScanOffer>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, ExecutionContext threadCtx) throws ApplicationException {

		ExecutionContext exeCtx = threadCtx != null ? threadCtx : Akka.system().dispatchers().defaultGlobalDispatcher();
		
		Promise<Map<String, RetailScanOffer>> promise = F.Promise.promise((Function0<Map<String, RetailScanOffer>>) () -> {
            return this.getItemDetails(itemMap);
        } , exeCtx);
		
		return promise;
	}
	
	private Map<String, RetailScanOffer> getItemDetails(Map<String, ShoppingListItem> itemMap) 
			throws ApplicationException {
		
		List<Long> scanCodes = new ArrayList<Long>();
		
		for (Entry<String, ShoppingListItem> entry : itemMap.entrySet()) {

			scanCodes.add(Long.valueOf(entry.getKey()));
		}
		
		Map<String, RetailScanOffer> retailScanOfferList = convertKeysToString(retailScanCache
				.getRetailScanOfferDetailByScanCds(scanCodes.toArray(new Long[scanCodes.size()])));
		
		return retailScanOfferList;
		
	}
	
	public Map<String, RetailScanOffer> convertKeysToString(Map<Long, RetailScanOffer> offerMap) {
		
		 Map<String, RetailScanOffer> transformedOfferMap = new HashMap<String, RetailScanOffer>();
		 
		 Set<Long> keySet = offerMap.keySet();
		 Iterator<Long> itr = keySet.iterator();
		 while(itr.hasNext()) {
			 Long key = itr.next();
			 
			 transformedOfferMap.put(key.toString(), offerMap.get(key));
			 
		 }
		 
		 return transformedOfferMap;		 
		 
		
	}

}
