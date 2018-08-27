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

package com.safeway.app.emju.mylist.service.item;


import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collection;
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

import com.safeway.app.emju.allocation.dao.PurchasedItemDAO;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.model.WeeklyAddVO;

import play.Application;
import play.Configuration;
import play.Play;
import play.test.Helpers;

/* ***************************************************************************
 * NAME         : WSDetailsProvider.java
 *
 * SYSTEM       : J4UOfferServicesShared
 *
 * AUTHOR       : Puneet Saxena
 *
 * REVISION HISTORY
 *
 * Revision 0.0.0.0 Oct 10, 2017 psaxe00
 * Initial creation for J4UOfferServicesShared
 *
 ***************************************************************************/

/**
 *
 * @author psaxe00
 */
@RunWith(MockitoJUnitRunner.class)
public class WSDetailsProviderTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(WSDetailsProviderTest.class);
    private static Application fakeApp;
    private WSDetailsProvider wsDetailsProvider;
    @Mock
    private PurchasedItemDAO purchasedItemDAO;
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
    	wsDetailsProvider = new WSDetailsProvider();
    }
    
    
    @Test
    public void testgetItemDetailsForSuccess() throws Exception {
    	
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "10005");
    	
    	ShoppingListVO vo = new ShoppingListVO();
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = DateHelper.getISODate(new Date(), header.getTimeZone());
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("10001");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	
    	PreferredStore preferStore = new PreferredStore();
    	preferStore.setStoreId(10000);
    	preferStore.setTimeZone("America/Los_Angeles");
    	preferStore.setRegionId(20000);
    	preferStore.setPostalCode("12345");
    	header.setPreferredStore(preferStore);
    	vo.setHeaderVO(header);
    	
    	
    	Map<String, Map<String, List<AllocatedOffer>>> matchedOffers = new HashMap<>();
    	Map<String, List<AllocatedOffer>> list = new HashMap<>();
    	AllocatedOffer offer = new AllocatedOffer();
    	offer.setClipId(1001L);
    	offer.setOfferId(5000L);
    	list.put("YCS", Arrays.asList(new AllocatedOffer[] { offer }));
    	list.put("CC", Arrays.asList(new AllocatedOffer[] { offer }));
    	matchedOffers.put("500003", list);
    	matchedOffers.put("500004", list);
    	
    	Collection<ShoppingListItemVO> result = wsDetailsProvider.getItemDetails(itemMap, vo, matchedOffers);
    	assertNotNull(result);
    	
    }
    
    @Test
    public void test2getItemDetailsForSuccess() throws Exception {
    	
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "10005");
    	Map<String, WeeklyAddVO> weeklyAddMap = buildWeeklyAddVOMap("500001", "500002", "500003", "500004", "500005");
    	
    	ShoppingListVO vo = new ShoppingListVO();
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = DateHelper.getISODate(new Date(), header.getTimeZone());
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("10001");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	
    	PreferredStore preferStore = new PreferredStore();
    	preferStore.setStoreId(10000);
    	preferStore.setTimeZone("America/Los_Angeles");
    	preferStore.setRegionId(20000);
    	preferStore.setPostalCode("12345");
    	header.setPreferredStore(preferStore);
    	vo.setHeaderVO(header);
    	
    	Collection<ShoppingListItemVO> result = wsDetailsProvider.getItemDetails(weeklyAddMap, itemMap, vo);
    	assertNotNull(result);
    	
    }
    
    private Map<String, ShoppingListItem> buildShoppingListItem(final String... itemsIDs) {

    	Map<String, ShoppingListItem> listItems = new HashMap<String, ShoppingListItem>();
    	ShoppingListItem itemVo = null;
    	int i = 0;
        for (String item : itemsIDs) {
        	itemVo = new ShoppingListItem();
        	itemVo.setItemId(item);
        	if(i%2==0){
        		itemVo.setTtl(123);
        		itemVo.setItemTitle("title");
        	}
        	itemVo.setClipTs(new Date());
        	itemVo.setLastUpdTs(new Date());
        	itemVo.setCheckedId("y");
        	itemVo.setCategoryId(1);
        	itemVo.setStoreId(1234);
        	itemVo.setItemEndDate(new Date());
        	itemVo.setItemDesc("Test");
        	itemVo.setItemTitle("xyz");
        	itemVo.setItemImage("image");
        	listItems.put(item, itemVo);
        	
        	i++;
        }

        return listItems;
    }
    
    private Map<String, WeeklyAddVO> buildWeeklyAddVOMap(final String... offerIDs) {

    	Map<String, WeeklyAddVO> list = new HashMap<String, WeeklyAddVO>();
    	WeeklyAddVO vo = null;

    	for (String offer : offerIDs) {
        	vo = new WeeklyAddVO();
        	vo.setOfferId(offer.toString());
        	list.put(offer, vo);
        }

        return list;
    }
}
