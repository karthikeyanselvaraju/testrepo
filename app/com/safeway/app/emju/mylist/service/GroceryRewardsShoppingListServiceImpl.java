package com.safeway.app.emju.mylist.service;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Inject;
import com.safeway.app.emju.allocation.cliptracking.entity.MyListItemStatus;
import com.safeway.app.emju.allocation.cliptracking.service.OfferStatusService;
import com.safeway.app.emju.allocation.dao.GRAllocationDAO;
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferProgram;
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferStatus;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.StoreCache;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.Store;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.exception.FaultCodeBase;
import com.safeway.app.emju.helper.DataHelper;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.dao.ShoppingListDAO;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.helper.ExecutionContextHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.AllocatedOfferDetail;
import com.safeway.app.emju.mylist.model.GroceryRewardsVO;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.service.detail.GRItemDetailAsyncRetriever;

import play.Configuration;
import play.libs.F;
import play.libs.F.Function0;
import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

public class GroceryRewardsShoppingListServiceImpl implements GroceryRewardsShoppingListService {

	private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingListServiceImp.class);
	
	private GRAllocationDAO grAllocationDAO;
	private OfferDetailCache offerCache;
    private ShoppingListDAO shoppingListDAO;
    private OfferStatusService offerStatusService;
    private GRItemDetailAsyncRetriever grItemDetailAsyncRetriever;
    private StoreCache storeCache;
    
    private static final int DEFAULT_LIST_STATUS_TIMEOUT = 500;
    private static final String REEDEMED_OFFER_TIMEOUT_KEY = "gallery.allocation.reedemed.offer.timeout";
    private static int REDEEMED_OFFER_TIMEOUT;
	
	@Inject
	public GroceryRewardsShoppingListServiceImpl(final GRAllocationDAO grAllocationDAO,
			                                     final GRItemDetailAsyncRetriever grItemDetailAsyncRetriever,  
			                                     final OfferDetailCache offerCache,
			                                     final OfferStatusService offerStatusService,
			                                     final ShoppingListDAO shoppingListDAO,
			                                     final StoreCache storeCache,
			                                     final Configuration playConfig) {

		this.grAllocationDAO = grAllocationDAO;
		this.grItemDetailAsyncRetriever = grItemDetailAsyncRetriever;
		this.offerCache = offerCache;
		this.shoppingListDAO = shoppingListDAO;
		this.offerStatusService = offerStatusService;
		this.storeCache = storeCache;
		REDEEMED_OFFER_TIMEOUT = playConfig.getInt(REEDEMED_OFFER_TIMEOUT_KEY, DEFAULT_LIST_STATUS_TIMEOUT);
	}


	@Override
	public GroceryRewardsVO getGroceryRewardsShoppingList(ShoppingListVO shoppingListVO) throws ApplicationException {

		LOGGER.info(">>> getGroceryRewardsShoppingList");
		
		GroceryRewardsVO groceryRewards = new GroceryRewardsVO();
		HeaderVO headerVO = shoppingListVO.getHeaderVO();
		String clientTimezone = headerVO.getTimeZone();
		Long clientDBCurrDtInMS = DataHelper.getCurrTsInTzAsDBTzMs(clientTimezone);
		Date currentClientDt = new Date(clientDBCurrDtInMS);
		String customerGUID = headerVO.getSwycustguid();
        Long householdId = new Long(headerVO.getSwyhouseholdid());
		Integer storeId = new Integer(headerVO.getParamStoreId());
		ExecutionContext daoContext = ExecutionContextHelper.getContext("play.akka.actor.dao-context");
		
		Store store = storeCache.getStore(storeId);
		PreferredStore preferredStore = new PreferredStore();
		if(store != null){
			preferredStore.setStoreId(store.getStoreId());
			preferredStore.setPostalCode(store.getZipCode());
			preferredStore.setRegionId(store.getRegionId());
		}
		headerVO.setPreferredStore(preferredStore);
		
		Promise<List<String>> redeemedOffersPromise = F.Promise.promise((Function0<List<String>>) () -> {
	        	 List<String> redeemedOffers = new ArrayList<String>();
	        	 List<Long> foundOffers = offerStatusService.findRedeemedOffersForRemoval(householdId);
	        	 foundOffers.forEach((id) -> {
	        		 redeemedOffers.add(String.valueOf(id));
	        	 });
	        	 return redeemedOffers;
	        },daoContext);
		
		List<MyListItemStatus> myListItems = offerStatusService.findGroceryRewardsMyListItems(customerGUID, householdId, null);
		
		
		HashMap<String, ShoppingListItem> itemMap = new HashMap<String, ShoppingListItem>();
		
		for (MyListItemStatus myListItemStatus : myListItems) {
			
			ShoppingListItem shpItem = new ShoppingListItem();
			shpItem.setItemId(myListItemStatus.getItemId());
			shpItem.setDeleteTs(myListItemStatus.getDeleteTs());
			
			itemMap.put(myListItemStatus.getItemId(), shpItem);
		}
		// FILTER POSTAL AND ZIPCODE BASED CLIPPED GROCERY REWARD OFFERS
		Map<String, OfferDetail> offerDetailMap = grItemDetailAsyncRetriever.getDetails(null, itemMap, shoppingListVO);
    	LOGGER.info("Offers from cache count : " + offerDetailMap.size());
		
	    
    	List<String> redeemedOffers = redeemedOffersPromise.get(REDEEMED_OFFER_TIMEOUT);
    	offerDetailMap.keySet().removeAll(redeemedOffers);
		LOGGER.debug("GR offers after removing redeemed offers: " + offerDetailMap.keySet());
    	
    	
    	Map<Long, AllocatedOffer> offerMap = new HashMap<>();
		
		//BUILD OFFER 
	     if(offerDetailMap != null && !offerDetailMap.isEmpty()) {
		 
	    	ArrayList<String> offerIds =  new ArrayList<String>(offerDetailMap.keySet());
	    	 
	       for (String offerId : offerIds) {
		    	LOGGER.info("########### OfferId to process : " + offerId);
		    	 AllocatedOffer offer = new AllocatedOffer();
		    	 OfferDetail offerDetail = offerDetailMap.get(offerId);
		    	 
		    	 if(offerDetail != null) {
		    		 LOGGER.info("offer : " + offerId + " >>>>> DETAIL >>>> " + offerDetail.toString());
		    		 
		    		 if(!isOfferValidToDisplay(offerDetail, clientTimezone)){
		    			 continue;
		    		 }
		    		 
		    		 String offerPgm = offerDetail.getOfferProgramCd();
			         
			        
			         offer.setOfferId(Long.valueOf(offerId));
			         offer.setOfferPgm(offerPgm);
			         
			         offer.setOfferInfo(offerDetail);
				     offer.setExtlOfferId(offerDetail.getExternalOfferId());
				     offer.setOfferProvider(offerDetail.getServiceProviderNm());
				     offer.setOfferSubPgm(offerDetail.getOfferSubProgram());
				     
				     if(offerDetail.getLastUpdateTs() != null) {
				      Timestamp offerTs = new Timestamp(offerDetail.getLastUpdateTs().getTime());
				      offer.setOfferTs(offerTs);
				     }
				        
				     Date endDate = DataHelper.getDateInClientTimezone(clientTimezone, offerDetail.getOfferEffectiveEndDt());
				     offer.getOfferDetail().setOfferEndDt(DataHelper.getJSONDate(endDate));
				     Date startDate = DataHelper.getDateInClientTimezone(clientTimezone, offerDetail.getOfferEffectiveStartDt());
				     offer.getOfferDetail().setOfferStartDt(DataHelper.getJSONDate(startDate));
				    
					 AllocatedOfferDetail detail = offer.getOfferDetail();
			       
			         detail.setStartDt(offerDetail.getDisplayEffectiveStartDt());
			         detail.setEndDt(offerDetail.getOfferEffectiveEndDt());	
			         detail.setSavingsValue(offerDetail.getSavingValue());
			    
			         ShoppingListItem shpItem = itemMap.get(""+offerId);
		             offer.setDeleteTs(shpItem.getDeleteTs());
		             offerMap.put(Long.valueOf(offerId), offer);
		    	 }
			 }
	         
	         List<AllocatedOffer> offers = new ArrayList<>(offerMap.values());
	         groceryRewards.setOffers(offers.toArray(new AllocatedOffer[offers.size()]));
	  		 LOGGER.debug("Final My Grocery Reward Offers : " + offerMap.keySet());
	         
		  }
	     
		return groceryRewards;
	}

	private boolean isOfferValidToDisplay(OfferDetail offerDetail, String clientTimeZone){
		LOGGER.info("isOfferValidToDisplay >>> offerId: " + offerDetail.getOfferId());
		Date offerEndDate = offerDetail.getOfferEffectiveEndDt();
		Long clientDate = DateHelper.getClientCurrDateInDBLocaleMS(clientTimeZone);
	    Date currClientDate = new Date(clientDate);
	    
	    if(offerEndDate == null) {
	    	LOGGER.info("Offer >>> " + offerDetail.getOfferId() + " has a null End Date and will not be displayed");
	    	return false;
	    }
	    
	    if(currClientDate.after(offerEndDate)) {
	    	LOGGER.info("Offer >>> " + offerDetail.getOfferId() + " has Expired and will not be displayed ");
			return false;	
		} 
        return true;    
	}	
}
