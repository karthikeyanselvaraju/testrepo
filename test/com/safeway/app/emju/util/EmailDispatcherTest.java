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

package com.safeway.app.emju.util;


import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.safeway.app.emju.cache.StoreCache;
import com.safeway.app.emju.cache.entity.Store;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mail.domain.EmailInformation;
import com.safeway.app.emju.mail.service.EmailBroker;
import com.safeway.app.emju.mylist.email.EmailDispatcher;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.MailListVO;
import com.safeway.app.emju.mylist.model.ShoppingListGroup;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;

import play.Application;
import play.Configuration;
import play.Play;
import play.test.Helpers;

/* ***************************************************************************
 * NAME         : EmailDispatcherTest.java
 *
 * SYSTEM       : J4UOfferServicesShared
 *
 * AUTHOR       : Puneet Saxena
 *
 * REVISION HISTORY
 *
 * Revision 0.0.0.0 Oct 13, 2017 psaxe00
 * Initial creation for J4UOfferServicesShared
 *
 ***************************************************************************/

/**
 *
 * @author psaxe00
 */
@RunWith(MockitoJUnitRunner.class)
public class EmailDispatcherTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailDispatcherTest.class);
    private static Application fakeApp;
    private EmailDispatcher email;
    
    private String j4UImageUrl;
    private String ycsImageUrl;
    private String ycsImageExt;
    private String wsImageUrl;
    private String wsImageExt;
	
	// the savings suffix
	private static Map<String, String> savingsSuffix;
	// types
	private static Map<String, String> types;
	// usage limits
	private static Map<String, String> usageLimits;
	
	@Mock
	private EmailBroker emailBroker;
	@Mock
	private StoreCache storeCache;
	@Mock
	private List<ShoppingListItemVO> items;
	@Mock
	private MailListVO mailListVO;
	private String bannerId;
	@Mock
	private EmailInformation slNotification;
	private String ycsStoreId;
    
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
    	String [] itemids = {"1001","1002","1003","1004","1005","1006"};
    	items = getListOfShoppingListItemVO(itemids);
    	mailListVO = new MailListVO();
    	mailListVO.setToEmails(new String[]{"abc.com"});
    	bannerId = "111";
    	ycsStoreId = "222";
    	slNotification = new EmailInformation();
    	
    	email = new EmailDispatcher(emailBroker, storeCache, items, mailListVO, bannerId, 
    			slNotification, ycsStoreId, j4UImageUrl, ycsImageUrl, ycsImageExt, wsImageUrl, wsImageExt);
    	
    }
    
    
    @Test
    public void testrunForSuccess() throws Exception {
    	
    	List<String> sendList = Arrays.asList(new String[]{"1001","1002","1003","1004","1005","1006"});
    	ShoppingListGroup group = new ShoppingListGroup();
    	group.setItemIds(sendList);
    	ShoppingListGroup[] shoppingListGroup = {group};
    	mailListVO.setGroups(shoppingListGroup);
    	
    	// Mock Call
    	Store store = new Store();
    	store.setStoreId(222);
    	when(storeCache.getStoreDetailsById(anyVararg())).thenReturn(store);
    	
    	email.run();
    }
    
    private List<ShoppingListItemVO> getListOfShoppingListItemVO(final String... itemIds){
    	List<ShoppingListItemVO> list = new ArrayList<ShoppingListItemVO>();
    	ShoppingListItemVO item = null;
    	int i = 0;
        for (String itemId : itemIds) {
            item = new ShoppingListItemVO();
            item.setId(itemId);
            Calendar calendar = Calendar.getInstance();
            item.setStartDate(DateHelper.getISODate(calendar.getTime(), "America/Los_Angeles"));
            calendar.add(Calendar.DATE, 8);
            item.setEndDate(DateHelper.getISODate(calendar.getTime(), "America/Los_Angeles"));
            if(i == 0){
                item.setItemType("YCS");	
                item.setImage("JPEG");
                item.setSavingsCode("BG");
                item.setSavingsSubCode("B1G1");
            }
            else if(i == 1){
            	item.setItemType("SC");
            	item.setImage("JPEG");
            	item.setSavingsCode("MB");
                item.setSavingsSubCode("MB2");
            }
            else if (i ==2){
            	item.setItemType("WS");
            	item.setImage("JPEG");
            }
            else if(i == 3){
            	item.setItemType("ELP");
            	item.setImage("JPEG");
            }
            else if(i==4){
            	item.setItemType("YCS");
            	item.setImage("JPEG");
            	item.setSavingsCode("MB");
                item.setSavingsSubCode("MB2");
            }
            else if(i==5){
            	item.setItemType("UPC");
            	item.setImage("JPEG");
            	item.setQuantity("22");
            }
            item.setDescription("Description");
            item.setSavingsValue("123.00");
            item.setSummary("Summary");
            
            list.add(item);
            i++;
        }
    	return list;
    }
    
}
