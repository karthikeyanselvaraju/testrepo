package com.safeway.app.emju.mylist.service.item;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;

import com.google.inject.Singleton;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.mylist.constant.Constants;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.helper.DetailUtil;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.service.ItemDetailsProvider;

@Singleton
public class FFDetailsProvider implements ItemDetailsProvider<OfferDetail> {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(FFDetailsProvider.class);

	@Override
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, Map<String, Map<String, List<AllocatedOffer>>> matchedOffers)
					throws ApplicationException {
		
		LOGGER.debug("FF Items found: " + itemMap.keySet());
		Map<String, ShoppingListItemVO> manualItemsMap = new HashMap<String, ShoppingListItemVO>();
		for (Map.Entry<String, ShoppingListItem> entry : itemMap.entrySet()) {
			ShoppingListItemVO shoppingListItemVO = new ShoppingListItemVO();
			manualItemsMap.put(entry.getKey(),
					setManualDetails(itemMap.get(entry.getKey()), shoppingListItemVO, shoppingListVO));
		}

		return manualItemsMap.values();
	}
	
	private ShoppingListItemVO setManualDetails(final ShoppingListItem manualItem,
			final ShoppingListItemVO shoppingListItemVO, final ShoppingListVO shoppingListVO) {
		String details = shoppingListVO.getHeaderVO().getDetails();
		String clientTimezone = shoppingListVO.getHeaderVO().getTimeZone();
		shoppingListItemVO.setId(manualItem.getClipId());
		shoppingListItemVO.setItemType(manualItem.getItemTypeCd());
		boolean hasItemIdFilter = shoppingListVO.getItemIds() != null;

		if (Constants.YES.equalsIgnoreCase(details)) {
			
			shoppingListItemVO.setDescription("");
            shoppingListItemVO.setQuantity("");

			if (null != manualItem.getClipTs()) {
				shoppingListItemVO.setAddedDate(DateHelper.getISODate(manualItem.getClipTs(), clientTimezone));
			}
			if (null != manualItem.getLastUpdTs()) {
				// Fix for Production Issue LastUpdatedDate
				shoppingListItemVO.setLastUpdatedDate(DateHelper.getISODate(manualItem.getLastUpdTs(), clientTimezone));

			}

			String description = hasItemIdFilter ? DetailUtil.cleanExtraChars(manualItem.getItemDesc(), "%3F") 
					: manualItem.getItemDesc();
			shoppingListItemVO.setDescription(description);
			shoppingListItemVO.setQuantity(manualItem.getItemQuantity());
			if (null != manualItem.getCheckedId()) {
				shoppingListItemVO.setChecked(manualItem.getCheckedId().equalsIgnoreCase(Constants.YES));
			}

			if (null != manualItem.getCategoryId()) {
				shoppingListItemVO.setCategoryId(manualItem.getCategoryId().toString());
			}
		}

		return shoppingListItemVO;

	}

	@Override
	public Collection<ShoppingListItemVO> getItemDetails(Map<String, OfferDetail> offerDetailMap,
			Map<String, ShoppingListItem> itemMap, ShoppingListVO shoppingListVO) throws ApplicationException {

		return null;
	}

}
