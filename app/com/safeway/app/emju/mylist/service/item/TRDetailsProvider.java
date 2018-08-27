package com.safeway.app.emju.mylist.service.item;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Singleton;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.exception.FaultCodeBase;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

@Singleton
public class TRDetailsProvider extends OFRDetailsProvider<OfferDetail> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(TRDetailsProvider.class);

	@Override
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, Map<String, Map<String, List<AllocatedOffer>>> matchedOffers)
					throws ApplicationException {
		throw new UnsupportedOperationException();
	}
	
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, OfferDetail> offerDetailMap, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO)
					throws ApplicationException {
		
		LOGGER.debug("Returned offer detail key set= " + offerDetailMap.keySet());
		LOGGER.debug("After getting offer details from cache " + offerDetailMap);
		
		Map<String, ShoppingListItemVO> offerItemsMap = new HashMap<String, ShoppingListItemVO>();
		String clientTimeZone = null;
		Date offerEndDate = null;
		String offerId = null;
		String error = null;
		
		try {
			clientTimeZone = shoppingListVO.getHeaderVO().getPreferredStore().getTimeZone();
			Long clientDate = DateHelper.getClientCurrDateInDBLocaleMS(clientTimeZone);
			Date currClientDate = new Date(clientDate);
			
			for (Entry<String, OfferDetail> entry : offerDetailMap.entrySet()) {
	
				offerId = entry.getKey();
				OfferDetail offerDetail = entry.getValue();
				offerEndDate = offerDetail.getOfferEffectiveEndDt();
				
				if(offerEndDate != null) {
					if(currClientDate.after(offerEndDate)) {
						continue;
					}
				} else {
					error = "Error on offerId = " + offerId + " with OfferEffectiveEndDt = " + offerEndDate;
					LOGGER.error(FaultCodeBase.CACHE_READ_FAILURE, error, new Exception(error), false);
				}
				
				
				offerItemsMap.put(offerId.toString(),
						getOfferItemDefinitions(offerDetail, itemMap.get(offerId), shoppingListVO));
			}
		} catch (Exception e) {
			LOGGER.error("Exception-->TRDetailsProvider>>setOfferDetails-->  "
					+ e.getMessage() + " with clientTimeZone = " + clientTimeZone);
			throw new ApplicationException(FaultCodeBase.EMLS_UNABLE_TO_PROCESS, null, null);
		}
		LOGGER.info("TRDetailsProvider setOfferDetails <<");
		return offerItemsMap.values();
	}
	
	

}
