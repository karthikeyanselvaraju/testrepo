/* **************************************************************************
 * Copyright 2015 Albertsons Safeway.
 *
 * This document/file contains proprietary data that is the property of
 * Albertsons Safeway.  Information contained herein may not be used,
 * copied or disclosed in whole or in part except as permitted by a
 * written agreement signed by an officer of Albertsons Safeway.
 *
 * Unauthorized use, copying or other reproduction of this document/file
 * is prohibited by law.
 *
 ***************************************************************************/

package com.safeway.app.emju.mylist.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Session;
import com.safeway.app.emju.allocation.cliptracking.model.OfferClipStatus;
import com.safeway.app.emju.allocation.cliptracking.service.OfferStatusService;
import com.safeway.app.emju.allocation.dao.CCAllocationDAO;
import com.safeway.app.emju.allocation.dao.PDAllocationDAO;
import com.safeway.app.emju.allocation.dao.PurchasedItemDAO;
import com.safeway.app.emju.allocation.entity.PurchasedItem;
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferClassifiers;
import com.safeway.app.emju.allocation.partner.service.PartnerAllocationService;
import com.safeway.app.emju.allocation.pricing.dao.ClubPriceDAO;
import com.safeway.app.emju.allocation.pricing.dao.OfferStorePriceDAO;
import com.safeway.app.emju.allocation.pricing.entity.ClubPrice;
import com.safeway.app.emju.allocation.pricing.entity.OfferStorePrice;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.RetailScanCache;
import com.safeway.app.emju.cache.dao.RetailScanOfferDAO;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.RetailScanOffer;
import com.safeway.app.emju.cache.resilient.RetailScanCacheResilientImpl;
import com.safeway.app.emju.dao.connector.CassandraConnector;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.emju.redis.RedisCacheManager;

import play.Application;
import play.Configuration;
import play.Play;
import play.test.Helpers;

/* ***************************************************************************
 * NAME         : MatchOfferServiceImpTest.java
 *
 * SYSTEM       : J4UOfferServicesShared
 *
 * AUTHOR       : Puneet Saxena
 *
 * REVISION HISTORY
 *
 * Revision 0.0.0.0 Oct 9, 2017 psaxe00
 * Initial creation for J4UOfferServicesShared
 *
 ***************************************************************************/

/**
 *
 * @author psaxe00
 */
@RunWith(MockitoJUnitRunner.class)
public class MatchOfferServiceImpTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchOfferServiceImpTest.class);
    private static Application fakeApp;

    
    @Mock
    public MatchOfferServiceImp matchOfferServiceImp;
    @Mock
    private  PurchasedItemDAO purchasedItemDAO;
    @Mock
    private RetailScanCache retailScanCache;
    @Mock
    private PDAllocationDAO pdAllocationDAO;
    @Mock
	private CCAllocationDAO ccAllocationDAO;
    @Mock
	private OfferStorePriceDAO pricingDAO;
    @Mock
	private PartnerAllocationService partnerAllocationService;
    @Mock
    private OfferStatusService offerStatusService;
    @Mock
    private OfferDetailCache offerCache;
    @Mock
    private ClubPriceDAO clubPriceDao; 
    @Mock
    private Session session;
    @Mock
    private CassandraConnector connector;
    @Mock
    private BoundStatement bs;
    @Mock
    private RetailScanOfferDAO dao;
    @Mock
    private RetailScanCacheResilientImpl retailImpl;
    @Mock
    private RetailScanOfferDAO rtlScanOfferDAO;
    @Mock
    private RedisCacheManager redisCache;
    

    Configuration config = Play.application().configuration();
    
    @BeforeClass
    public static void startApp() {
    	LOGGER.debug("Application initializing...");
        fakeApp = Helpers.fakeApplication();
        Helpers.start(fakeApp);
        LOGGER.debug("Application started");
    }

    @AfterClass
    public static void stopApp() {
        Helpers.stop(fakeApp);
    }

    @Before
    public void setup() {
    	matchOfferServiceImp = new MatchOfferServiceImp(purchasedItemDAO, clubPriceDao, offerStatusService, retailScanCache, pdAllocationDAO,
    			ccAllocationDAO, offerCache, pricingDAO, partnerAllocationService);
    }
    
    
    @Test
    public void testgetRelatedOffersForSuccess() throws Exception {
    	
    	Map<Long, ShoppingListItem> itemsMap = buildShoppingListItemMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<Long, PurchasedItem> purchasedItems = buildPurchaseListItemMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<Long, ClubPrice> ycsPrices = buildClubPriceMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<Long, RetailScanOffer> retailScanOfferList = buildRetailScanOfferMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	List<Long> redeemedOfferIdList = Arrays.asList(new Long[] {400001L,40000L});
    	Map<Long, OfferClipStatus> offerClipStatus = buildOfferClipStatusMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<Long, OfferDetail> offerDetailMap = buildOfferDetailMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<Long, OfferStorePrice> pricingMap = buildOfferStorePriceMap(500001L, 500002L, 500003L, 500004L, 500005L);
    	
    	PreferredStore preferStore = new PreferredStore();
    	preferStore.setStoreId(10000);
    	preferStore.setTimeZone("America/Los_Angeles");
    	preferStore.setRegionId(20000);
    	preferStore.setPostalCode("12345");
    	
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = DateHelper.getISODate(new Date(), header.getTimeZone());
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("20000");
    	header.setSwycoremaclubcard("1234567890");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	header.setPreferredStore(preferStore);
    	try {
    		when(purchasedItemDAO.findItemsByHHIdAndScanCode(any(Long.class), any(List.class))).thenReturn(purchasedItems);
    		when(clubPriceDao.findItemPrices(any(String.class), any(Integer.class), any(List.class))).thenReturn(ycsPrices);
    		when(retailScanCache.getRetailScanOfferDetailByScanCds(anyVararg())).thenReturn(retailScanOfferList);
    		when(offerStatusService.findRedeemedOffersForRemoval(any(Long.class))).thenReturn(redeemedOfferIdList);
    		when(offerStatusService.findOfferClipStatus(any(String.class),any(Long.class),any(List.class),any(String.class))).thenReturn(offerClipStatus);
    		when(offerCache.getOfferDetailsByIds(anyVararg())).thenReturn(offerDetailMap);
    		when(pricingDAO.findOfferPrices(any(Integer.class), any(List.class))).thenReturn(pricingMap);
		} catch (Exception e) {}
    	
    	Map<String, Map<String, List<AllocatedOffer>>> list = matchOfferServiceImp.getRelatedOffers(itemsMap, header);
    	assertNotNull(list);
    	
    	
    }
    
    private Map<Long, ShoppingListItem> buildShoppingListItemMap(final Long... clipIds) {

    	Map<Long, ShoppingListItem> listShopping = new HashMap<Long, ShoppingListItem>();
    	ShoppingListItem shopItem = null;

        for (Long clipId : clipIds) {
        	shopItem = new ShoppingListItem();
        	shopItem.setClipId(clipId.toString());
        	shopItem.setClipTs(new Date());
        	listShopping.put(clipId, shopItem);
        }

        return listShopping;
    }
    
    private Map<Long, PurchasedItem> buildPurchaseListItemMap(final Long... retailScanCds) {

    	Map<Long, PurchasedItem> listPurchase = new HashMap<Long, PurchasedItem>();
    	PurchasedItem purchaseItem = null;

        for (Long retailScanId : retailScanCds) {
        	purchaseItem = new PurchasedItem();
        	purchaseItem.setRetailScanCd(retailScanId);
        	listPurchase.put(retailScanId, purchaseItem);
        }

        return listPurchase;
    }
    
    private Map<Long, ClubPrice> buildClubPriceMap(final Long... retailScanCds) {

    	Map<Long, ClubPrice> listClubPrice = new HashMap<Long, ClubPrice>();
    	ClubPrice clubPrice = null;

    	int i =0;
        for (Long retailScanId : retailScanCds) {
        	clubPrice = new ClubPrice();
        	clubPrice.setRetailScanCd(retailScanId);
        	clubPrice.setPromotionPrice(1.0);
        	clubPrice.setRetailPrice(2.0);
        	clubPrice.setSavings(3.0);
        	Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 2);
        	clubPrice.setStartDt(calendar.getTime());
        	if(i%2==0){
        		clubPrice.setPriceMethod(OfferClassifiers.PRICE_METHOD_TYPE_BOGO);
        		clubPrice.setPriceMethodSubType(OfferClassifiers.PRICE_METHOD_SUB_TYPE_B1G1);
        	}
        	else{
        		clubPrice.setPriceMethod(OfferClassifiers.PRICE_METHOD_TYPE_MB);
        		clubPrice.setPriceMethodSubType(OfferClassifiers.PRICE_METHOD_SUB_TYPE_MB2);
        	}
        	listClubPrice.put(retailScanId, clubPrice);
        	i++;
        }

        return listClubPrice;
    }
    
    private Map<Long, RetailScanOffer> buildRetailScanOfferMap(final Long... offerIDs) {

    	Map<Long, RetailScanOffer> listRetailScanOffer = new HashMap<Long, RetailScanOffer>();
    	RetailScanOffer offer = null;
    	List<Long> offerIdList = Arrays.asList(new Long[] {400001L,40000L});

        for (Long offerId : offerIDs) {
        	offer = new RetailScanOffer();
        	offer.setOfferIdList(offerIdList);
        	offer.setRetailScanCd(offerId);
        	listRetailScanOffer.put(offerId, offer);
        }

        return listRetailScanOffer;
    }
    
    private Map<Long, OfferClipStatus> buildOfferClipStatusMap(final Long... OfferIDs) {

    	Map<Long, OfferClipStatus> listOffer = new HashMap<Long, OfferClipStatus>();
    	OfferClipStatus offer = null;

        for (Long offerId : OfferIDs) {
        	offer = new OfferClipStatus();
        	offer.setOfferId(offerId);
        	listOffer.put(offerId, offer);
        }

        return listOffer;
    }
    
    private Map<Long, OfferDetail> buildOfferDetailMap(final Long... OfferIDs) {

    	Map<Long, OfferDetail> listOffer = new HashMap<Long, OfferDetail>();
    	OfferDetail offer = null;

        for (Long offerId : OfferIDs) {
        	offer = new OfferDetail();
        	offer.setOfferId(offerId);
        	offer.setOfferStatusTypeId("A");
        	Calendar calendar = Calendar.getInstance();
        	calendar.add(Calendar.DATE, -1);
        	offer.setDisplayEffectiveStartDt(calendar.getTime());
            calendar.add(Calendar.DATE, 2);
        	offer.setDisplayEffectiveEndDt(calendar.getTime());
        	offer.setLastUpdateTs(calendar.getTime());
        	offer.setOfferPrice(5.0);
        	offer.setPrimaryCategoryId(99);
        	listOffer.put(offerId, offer);
        }

        return listOffer;
    }
    
    private Map<Long, OfferStorePrice> buildOfferStorePriceMap(final Long... OfferIDs) {

    	Map<Long, OfferStorePrice> listOffer = new HashMap<Long, OfferStorePrice>();
    	OfferStorePrice offer = null;

        for (Long offerId : OfferIDs) {
        	offer = new OfferStorePrice();
        	offer.setOfferId(offerId);
        	listOffer.put(offerId, offer);
        }

        return listOffer;
    }
}
