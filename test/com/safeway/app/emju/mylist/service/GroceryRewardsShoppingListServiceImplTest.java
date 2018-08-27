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


import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
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
import com.safeway.app.emju.allocation.cliptracking.entity.MyListItemStatus;
import com.safeway.app.emju.allocation.cliptracking.model.OfferClipStatus;
import com.safeway.app.emju.allocation.cliptracking.service.OfferStatusService;
import com.safeway.app.emju.allocation.dao.CCAllocationDAO;
import com.safeway.app.emju.allocation.dao.GRAllocationDAO;
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
import com.safeway.app.emju.cache.StoreCache;
import com.safeway.app.emju.cache.dao.RetailScanOfferDAO;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.RetailScanOffer;
import com.safeway.app.emju.cache.entity.Store;
import com.safeway.app.emju.cache.resilient.RetailScanCacheResilientImpl;
import com.safeway.app.emju.dao.connector.CassandraConnector;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.dao.ShoppingListDAO;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.GroceryRewardsVO;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.service.detail.GRItemDetailAsyncRetriever;
import com.safeway.emju.redis.RedisCacheManager;

import play.Application;
import play.Configuration;
import play.Play;
import play.libs.Akka;
import play.test.Helpers;
import scala.concurrent.ExecutionContext;

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
public class GroceryRewardsShoppingListServiceImplTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GroceryRewardsShoppingListServiceImplTest.class);
    private static Application fakeApp;
    
    private GroceryRewardsShoppingListServiceImpl service;
    @Mock
    private GRAllocationDAO grAllocationDAO;
    @Mock
    private OfferDetailCache offerCache;
    @Mock
    private ShoppingListDAO shoppingListDAO;
    @Mock
    private OfferStatusService offerStatusService;
    @Mock
    private GRItemDetailAsyncRetriever grItemDetailAsyncRetriever;
    @Mock
    private StoreCache storeCache;
    @Mock
    private ExecutionContext daoContext;
    

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
    	service = new GroceryRewardsShoppingListServiceImpl(grAllocationDAO, grItemDetailAsyncRetriever, offerCache, offerStatusService, shoppingListDAO, storeCache,config);
    	daoContext = Akka.system().dispatchers().defaultGlobalDispatcher();
    }
    
    
    @Test
    public void testgetGroceryRewardsShoppingListForSuccess() throws Exception {
    	
    	List<MyListItemStatus> myListItems = buildMyListItemStatusList(500001L, 500002L, 500003L, 500004L, 500005L);
    	Map<String, OfferDetail> offerDetailMap = buildOfferDetailMap("500001", "500002", "500003", "500004", "500005");
    	Store store = new Store();
    	store.setStoreId(1000);
    	store.setRegionId(444);
    	store.setZipCode("28262");
    	
    	ShoppingListVO vo = new ShoppingListVO();
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = DateHelper.getISODate(new Date(), header.getTimeZone());
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("10001");
    	header.setSwycustguid("4567");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	
    	vo.setHeaderVO(header);
    	
    	when(storeCache.getStore(anyVararg())).thenReturn(store);
    	when(offerStatusService.findGroceryRewardsMyListItems(any(String.class),any(Long.class),any(Integer.class))).thenReturn(myListItems);
    	when(grItemDetailAsyncRetriever.getDetails(any(String.class), any(Map.class), any(ShoppingListVO.class))).thenReturn(offerDetailMap);
    	
    	GroceryRewardsVO result = service.getGroceryRewardsShoppingList(vo);

    	assertNotNull(result);
    	//assertEquals(result.getOffers()[0].getOfferId(), offerDetailMap.get(500005L).getOfferId());
    }
    
    private List<MyListItemStatus> buildMyListItemStatusList(final Long... itemIDs) {

    	List<MyListItemStatus> list = new ArrayList<MyListItemStatus>();
    	MyListItemStatus item = null;

        for (Long itemId : itemIDs) {
        	item = new MyListItemStatus();
        	item.setItemId(itemId.toString());
        	list.add(item);
        }

        return list;
    }
    
    private Map<String, OfferDetail> buildOfferDetailMap(final String... OfferIDs) {

    	Map<String, OfferDetail> listOffer = new HashMap<String, OfferDetail>();
    	OfferDetail offer = null;

        for (String offerId : OfferIDs) {
        	offer = new OfferDetail();
        	offer.setOfferId(Long.valueOf(offerId));
        	offer.setOfferStatusTypeId("A");
        	Calendar calendar = Calendar.getInstance();
        	calendar.add(Calendar.DATE, -1);
        	offer.setOfferEffectiveEndDt(calendar.getTime());
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
    
}
