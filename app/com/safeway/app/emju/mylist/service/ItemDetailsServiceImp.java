package com.safeway.app.emju.mylist.service;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.safeway.app.emju.allocation.helper.OfferConstants.ItemType;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.ExecutionContextHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

@SuppressWarnings({ "rawtypes", "unchecked" })
@Singleton
public class ItemDetailsServiceImp implements ItemDetailsService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ItemDetailsServiceImp.class);
	
	private Map<String, ItemDetailsProvider> detailsProviderMap;
	private Map<String, ItemDetailAsyncRetriever> asyncDetailProviderMap;
	
	@Inject
	public ItemDetailsServiceImp(
			@Named("UPC") ItemDetailsProvider upcProvider, 
			@Named("CC") ItemDetailsProvider ccProvider,
			@Named("PD") ItemDetailsProvider pdProvider,
			@Named("YCS") ItemDetailsProvider ycsProvider,
			@Named("MCS") ItemDetailsProvider mcsProvider,
			@Named("FF") ItemDetailsProvider ffProvider,
			@Named("WS") ItemDetailsProvider wsProvider,
			@Named("ELP") ItemDetailsProvider elpProvider,
			@Named("REC") ItemDetailsProvider recProvider,
			@Named("GR") ItemDetailsProvider grProvider,
			@Named("TR") ItemDetailsProvider trProvider,
			@Named("UPC") ItemDetailAsyncRetriever upcAsyncProvider,
			@Named("CC") ItemDetailAsyncRetriever ccAsyncProvider,
			@Named("PD") ItemDetailAsyncRetriever pdAsyncProvider,
			@Named("YCS") ItemDetailAsyncRetriever ycsAsyncProvider,
			@Named("WS") ItemDetailAsyncRetriever wsAsyncProvider,
			@Named("GR") ItemDetailAsyncRetriever grAsyncProvider,
			@Named("TR") ItemDetailAsyncRetriever trAsyncProvider) {
		
		detailsProviderMap = new HashMap<String, ItemDetailsProvider>();
		detailsProviderMap.put(ItemType.UPC, upcProvider);
		detailsProviderMap.put(ItemType.CC, ccProvider);
		detailsProviderMap.put(ItemType.PD, pdProvider);
		detailsProviderMap.put(ItemType.YCS, ycsProvider);
		detailsProviderMap.put(ItemType.MCS, mcsProvider);
		detailsProviderMap.put(ItemType.FF, ffProvider);
		detailsProviderMap.put(ItemType.WS, wsProvider);
		detailsProviderMap.put(ItemType.ELP, elpProvider);
		detailsProviderMap.put(ItemType.REC, recProvider);
		detailsProviderMap.put(ItemType.GR, grProvider);
		detailsProviderMap.put(ItemType.TR, trProvider);
		
		asyncDetailProviderMap = new HashMap<String, ItemDetailAsyncRetriever>();
		asyncDetailProviderMap.put(ItemType.UPC, upcAsyncProvider);
		asyncDetailProviderMap.put(ItemType.CC, ccAsyncProvider);
		asyncDetailProviderMap.put(ItemType.PD, pdAsyncProvider);
		asyncDetailProviderMap.put(ItemType.YCS, ycsAsyncProvider);
		asyncDetailProviderMap.put(ItemType.WS, wsAsyncProvider);
		asyncDetailProviderMap.put(ItemType.GR, grAsyncProvider);
		asyncDetailProviderMap.put(ItemType.TR, trAsyncProvider);
	}

	@Override
	public Collection<ShoppingListItemVO> setItemDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, Map<String, Map<String, List<AllocatedOffer>>> matchedOffers)
					throws ApplicationException {
		
		LOGGER.debug("ItemDetailsServiceImp: setting item details for item type " + itemType);
		ItemDetailsProvider itemDetailProvider = detailsProviderMap.get(itemType);
		LOGGER.debug("Item Detail Provider = " + itemDetailProvider);
		return itemDetailProvider.getItemDetails(itemMap, shoppingListVO, matchedOffers);
	}
	
	

	@Override
	public Collection<ShoppingListItemVO> setItemDetails(String itemType, Map<Long, ?> itemDetailMap,
			Map<String, ShoppingListItem> itemMap, ShoppingListVO shoppingListVO) throws ApplicationException {

		ItemDetailsProvider itemDetailProvider = detailsProviderMap.get(itemType);
		return itemDetailProvider.getItemDetails(itemDetailMap, itemMap, shoppingListVO);
	}

	@Override
	public Promise<Map<Long, ?>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException {
		
		ExecutionContext serviceContext = ExecutionContextHelper.getContext("play.akka.actor.detail-context");
		ItemDetailAsyncRetriever itemDetailAsyncRetriever = asyncDetailProviderMap.get(itemType);
		return itemDetailAsyncRetriever.getAsyncDetails(itemType, itemMap, shoppingListVO, serviceContext);
		
	}

	@Override
	public Map<Long, ?> getDetailsPromiseResult(String itemType, Promise<Map<Long, ?>> promiseItemDetail)
			throws ApplicationException {
		
		ItemDetailAsyncRetriever itemDetailAsyncRetriever = asyncDetailProviderMap.get(itemType);
		return itemDetailAsyncRetriever.getDetailsPromiseResult(promiseItemDetail);
	}

}
