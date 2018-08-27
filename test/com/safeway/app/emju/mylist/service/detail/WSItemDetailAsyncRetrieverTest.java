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

package com.safeway.app.emju.mylist.service.detail;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
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

import com.safeway.app.emju.allocation.exception.OfferServiceException;
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferProgram;
import com.safeway.app.emju.cache.CacheAccessException;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.cache.WeeklyAddCache;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.model.WeeklyAddVO;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.Application;
import play.Configuration;
import play.inject.guice.GuiceApplicationBuilder;
import play.libs.Akka;
import play.libs.F.Promise;
import play.test.Helpers;
import scala.concurrent.ExecutionContext;

/**
 *
 * @author psaxe00
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class WSItemDetailAsyncRetrieverTest {

	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }
    private final static Logger LOGGER = LoggerFactory.getLogger(WSItemDetailAsyncRetrieverTest.class);

    private static Application fakeApp;
    private WSItemDetailAsyncRetriever retriever;
    @Mock
	private WeeklyAddCache cache;
    private static Configuration configuration;
    

    @BeforeClass
    public static void start() {

    	LOGGER.debug("Application initializing...");
        Config config = ConfigFactory.parseFile(new File("test/gallery-test.conf"));
        config = ConfigFactory.load(config);
        configuration = new Configuration(config);
        fakeApp = new GuiceApplicationBuilder().configure(configuration).build();
        fakeApp = Helpers.fakeApplication();
        Helpers.start(fakeApp);
        LOGGER.debug("Application started");
    }

    @AfterClass
    public static void stop() {
        Helpers.stop(fakeApp);
    }

    @Before
    public void setup() throws CacheAccessException, OfferServiceException {
    		
    	retriever = new WSItemDetailAsyncRetriever(cache);

    }

    @Test
    public void testgetAsyncDetailsForSuccess() throws Exception {
    	
    	String itemType = "CC";
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "500005");
    	
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
    	
        List<String> offerIds = Arrays.asList("500001", "500002", "500003", "500004", "500005");
    	Map<String, WeeklyAddVO> offers = getWeeklyAddVOOffers(offerIds);
    	when(cache.getWeeklyAddByOfferId(offerIds)).thenReturn(offers);
    	
    	Promise<Map<String, WeeklyAddVO>> result = retriever.getAsyncDetails(itemType, itemMap, vo);
    	assertNotNull(result);
    }
    
    @Test
    public void testgetAsyncDetailsWithContextForSuccess() throws Exception {
    	
    	String itemType = "CC";
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "500005");
    	
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
    	
        List<String> offerIds = Arrays.asList("500001", "500002", "500003", "500004", "500005");
    	Map<String, WeeklyAddVO> offers = getWeeklyAddVOOffers(offerIds);
    	when(cache.getWeeklyAddByOfferId(offerIds)).thenReturn(offers);
    	
    	ExecutionContext exeCtx = Akka.system().dispatchers().defaultGlobalDispatcher();
    	Promise<Map<String, WeeklyAddVO>> result = retriever.getAsyncDetails(itemType, itemMap, vo,exeCtx);
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
        		itemVo.setItemTypeCd(OfferProgram.PD);
        	}
        	else{
        		itemVo.setItemTypeCd(OfferProgram.MF);
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
    
    private Map<String, WeeklyAddVO> getWeeklyAddVOOffers(final List<String> offerIds) {

        Map<String, WeeklyAddVO> offers = new HashMap<String, WeeklyAddVO>();

        for(String offerId: offerIds) {
        	WeeklyAddVO vo = new WeeklyAddVO();
            vo.setOfferId(offerId);
            offers.put(offerId, vo);
        }

        return offers;
    }
    
}

