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
public class PDDetailsProvider extends OFRDetailsProvider<OfferDetail> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(OFRDetailsProvider.class);

	@Override
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, Map<String, Map<String, List<AllocatedOffer>>> matchedOffers)
					throws ApplicationException {
		
		return null;
	}

	@Override
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, OfferDetail> offerDetailMap,
			Map<String, ShoppingListItem> itemMap, ShoppingListVO shoppingListVO) throws ApplicationException {
		
		LOGGER.debug("After getting offer details from cache " + offerDetailMap);
		Map<String, ShoppingListItemVO> offerItemsMap = new HashMap<String, ShoppingListItemVO>();
		String clientTimeZone = shoppingListVO.getHeaderVO().getPreferredStore().getTimeZone();
		
		Long clientDate = DateHelper.getClientCurrDateInDBLocaleMS(clientTimeZone);
		Date currClientDate = new Date(clientDate);
		
		try{
			for (Entry<String, OfferDetail> entry : offerDetailMap.entrySet()) {
	
				String offerId = entry.getKey();
				OfferDetail offerDetail = entry.getValue();
				if(offerDetail.getOfferEffectiveEndDt() == null || 
						currClientDate.after(offerDetail.getOfferEffectiveEndDt())) {
					continue;
				}
				
				offerItemsMap.put(offerId.toString(),
						getOfferItemDefinitions(offerDetail, itemMap.get(offerId), shoppingListVO));
			}
		} catch (Exception e) {
			LOGGER.error("Exception-->PDDetailsProvider>>setOfferDetails-->  "
					+ e.getMessage());
			throw new ApplicationException(FaultCodeBase.EMLS_UNABLE_TO_PROCESS, null, null);
		}
		LOGGER.info("ShoppingListService setOfferDetails <<");
		return offerItemsMap.values();
	}

}
