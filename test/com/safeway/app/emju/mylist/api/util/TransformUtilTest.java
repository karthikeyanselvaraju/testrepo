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

package com.safeway.app.emju.mylist.api.util;


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
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
import com.safeway.app.emju.mobile.model.ClientRequestInfo;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.model.WeeklyAddVO;
import com.safeway.app.emju.mylist.purchasehistory.parser.PurchaseHistoryRequest;

import play.Application;
import play.Configuration;
import play.Play;
import play.test.Helpers;

/* ***************************************************************************
 * NAME         : TransformUtil.java
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
public class TransformUtilTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TransformUtilTest.class);
    private static Application fakeApp;
    private TransformUtil util;
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
    	util = new TransformUtil();
    }
    
    
    @Test
    public void testgetShoppingListVO() throws Exception {
    	ClientRequestInfo info = buildClientRequestInfo();
    	ShoppingListVO vo = new ShoppingListVO();
    	HeaderVO headerVO = new HeaderVO();
    	headerVO.setSwycustguid(info.getCustomerGUID());
    	headerVO.setSwyhouseholdid(info.getHouseholdId().toString());
    	headerVO.setSwycoremaclubcard(info.getClubCard().toString());
    	headerVO.setPostalcode(info.getPostalCode());
    	headerVO.setAppKey(info.getAppId());
    	headerVO.setAppVersion(info.getAppUser());
    	Map<String, boolean[]> versionMap = new HashMap<String, boolean[]>();
		versionMap.put("1.0", new boolean[] { false, true, false });
		versionMap.put("1.1", new boolean[] { true, true, true });
    	headerVO.setVersionValues(versionMap.getOrDefault(headerVO.getAppVersion().trim(), 
				new boolean[] { true, true, true }));
    	headerVO.setParamStoreId(Integer.toString(info.getStoreId()));
		headerVO.setBannner(info.getBanner());
		headerVO.setLoggedUserId(info.getAppUser());
		headerVO.setSessionToken(info.getSessionToken());
		headerVO.setSwyccpricezone(info.getPriceZone());
		vo.setHeaderVO(headerVO);
    	
		ShoppingListVO result = util.getShoppingListVO(info);
    	assertNotNull(result);
    	assertNotNull(result.getHeaderVO());
    	assertEquals(result.getHeaderVO().getSwyhouseholdid(), vo.getHeaderVO().getSwyhouseholdid());
		
    }
    
    @Test
    public void testgetPurchaseHistoryRequest() throws Exception {
    	ClientRequestInfo request = buildClientRequestInfo();
    	PurchaseHistoryRequest purchaseHistoryRequest = new PurchaseHistoryRequest();
    	purchaseHistoryRequest.setAppId(request.getAppId());
		purchaseHistoryRequest.setAppUser(request.getAppUser());
		purchaseHistoryRequest.setAppVersion(request.getAppVersion());
		purchaseHistoryRequest.setBanner(request.getBanner());
		purchaseHistoryRequest.setClubCard(request.getClubCard());
		purchaseHistoryRequest.setPostalCode(request.getPostalCode());
		purchaseHistoryRequest.setSessionToken(request.getSessionToken());

		purchaseHistoryRequest.setRegionId(request.getRegionId());
		purchaseHistoryRequest.setStoreId(request.getStoreId());
		purchaseHistoryRequest.setCustomerGUID(request.getCustomerGUID());
		purchaseHistoryRequest.setHouseholdId(request.getHouseholdId());
		purchaseHistoryRequest.setTimezone(request.getTimeZone());
		
		PurchaseHistoryRequest result = util.getPurchaseHistoryRequest(request);
		assertNotNull(result);
		assertEquals(result.getHouseholdId(), purchaseHistoryRequest.getHouseholdId());
    }
    
    private ClientRequestInfo buildClientRequestInfo() {
    	ClientRequestInfo info = new ClientRequestInfo();
    	info.setCustomerGUID("10000");
    	info.setHouseholdId(200007890L);
    	info.setClubCard(5000076868L);
    	info.setPostalCode("432432");
    	info.setSessionToken("43rf");
    	info.setBanner("324542");
    	info.setAppId("123");
    	info.setAppVersion("1.1");
    	info.setStoreId(40000);
    	info.setAppUser("puneet");
    	info.setPriceZone("4342.00");
    	
    	return info;
    }
    
    
    
}
