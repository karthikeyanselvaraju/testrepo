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


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Calendar;
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
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferProgram;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.exception.FaultCodeBase;
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
 * NAME         : GRDetailsProvider.java
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
public class GRDetailsProviderTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GRDetailsProviderTest.class);
    private static Application fakeApp;
    private GRDetailsProvider grDetailsProvider;
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
    	grDetailsProvider = new GRDetailsProvider();
    }
    
    
    @Test
    public void testgetItemDetailsForSuccess() throws Exception {
    	
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "500005");
    	Map<String, OfferDetail> offerDetailMap = buildOfferDetailMap("500001", "500002", "500003", "500004", "500005");
    	
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
    	
    	Collection<ShoppingListItemVO> result = grDetailsProvider.getItemDetails(offerDetailMap,itemMap, vo);
    	assertNotNull(result);
    	
    }
    
    @Test
    public void testgetItemDetailsFaliureWithOfferEndDate() throws Exception {
    	
    	Map<String, ShoppingListItem> itemMap = buildShoppingListItem("500001", "500002", "500003", "500004", "500005");
    	Map<String, OfferDetail> offerDetailMap = new HashMap<String, OfferDetail>();
    	OfferDetail offer = new OfferDetail();
    	offer.setOfferId(50001L);
    	offerDetailMap.put("50001", offer);
    	
    	
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
    	
    	
    	try {
    		grDetailsProvider.getItemDetails(offerDetailMap,itemMap, vo);
            fail("Error Expected");
        }
        catch (ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
            assertEquals("Fault Code should match", FaultCodeBase.EMLS_UNABLE_TO_PROCESS, e.getFaultCode());
        }
    	
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
    
    private Map<String, OfferDetail> buildOfferDetailMap(final String... OfferIDs) {

    	Map<String, OfferDetail> listOffer = new HashMap<String, OfferDetail>();
    	OfferDetail offer = null;

        for (String offerId : OfferIDs) {
        	offer = new OfferDetail();
        	offer.setOfferId(Long.valueOf(offerId));
        	offer.setOfferStatusTypeId("A");
        	Calendar calendar = Calendar.getInstance();
        	calendar.add(Calendar.DATE, -1);
        	offer.setDisplayEffectiveStartDt(calendar.getTime());
            calendar.add(Calendar.DATE, 2);
        	offer.setDisplayEffectiveEndDt(calendar.getTime());
        	offer.setLastUpdateTs(calendar.getTime());
        	offer.setOfferPrice(5.0);
        	offer.setPrimaryCategoryId(99);
        	offer.setOfferEffectiveStartDt(calendar.getTime());
        	calendar.add(Calendar.DATE, 1);
        	offer.setOfferEffectiveEndDt(calendar.getTime());
        	offer.setOfferProgramCd("07");
        	offer.setPriceMethodCd("AAA");
        	offer.setProdDsc1("1");
        	offer.setProdDsc2("2");
        	offer.setTitleDsc1("t1");
        	offer.setTitleDsc2("t2");
        	offer.setTitleDsc3("t3");
        	offer.setPriceValue1("123");
        	offer.setSavingValue("234");
        	offer.setDisclaimer("Dis");
        	listOffer.put(offerId, offer);
        }

        return listOffer;
    }
}
