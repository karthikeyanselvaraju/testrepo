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
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

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
import com.safeway.app.emju.allocation.cliptracking.dao.OfferStatusDAO;
import com.safeway.app.emju.allocation.cliptracking.service.OfferStatusService;
import com.safeway.app.emju.allocation.pricing.dao.ClubPriceDAO;
import com.safeway.app.emju.cache.CategoryCache;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.StoreCache;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.Store;
import com.safeway.app.emju.dao.connector.CassandraConnector;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.exception.FaultCodeBase;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mail.service.EmailBroker;
import com.safeway.app.emju.mylist.dao.ShoppingListDAO;
import com.safeway.app.emju.mylist.dao.StoreDAO;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.MailListVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListGroup;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.Application;
import play.Configuration;
import play.Play;
import play.libs.F.Promise;
import play.test.Helpers;

/* ***************************************************************************
 * NAME         : ShoppingListServiceImp.java
 *
 * SYSTEM       : J4UOfferServicesShared
 *
 * AUTHOR       : Puneet Saxena
 *
 * REVISION HISTORY
 *
 * Revision 0.0.0.0 Oct 4, 2017 psaxe00
 * Initial creation for J4UOfferServicesShared
 *
 ***************************************************************************/

/**
 *
 * @author psaxe00
 */
@RunWith(MockitoJUnitRunner.class)
public class ShoppingListServiceImpTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ShoppingListServiceImpTest.class);
    private static Application fakeApp;

    
    @Mock
    public ShoppingListServiceImp shoppingListServiceImp;
    @Mock
    private ShoppingListDAO shoppingListDAO;
    @Mock
    private OfferStatusService offerStatusService;
    @Mock
    private MatchOfferSevice matchOfferService; 
    @Mock
    private ItemDetailsService itemDetailsService; 
    @Mock
    private OfferDetailCache offerCache;
    @Mock
    private ClubPriceDAO clubPriceDao; 
    @Mock
    private CategoryCache categoryCache; 
    @Mock
    private StoreCache storeCache;
    @Mock
    private EmailBroker emailBroker;
    @Mock
    private StoreDAO storeDAO;
    @Mock
    public OfferStatusDAO offerStatusDAO;
    @Mock
    private Session session;
    private CassandraConnector connector;
    @Mock
    private BoundStatement bs;
    @Mock
    private PreferredStore preferredStore;
    @Mock
    private Map<String, Map<String, List<AllocatedOffer>>> matchedOffers;
    @Mock
    private ItemDetailAsyncRetriever itemDetailAsyncRetriever;
    
    @Mock
    private Promise<Map<Long, ?>> promise;
    @Mock
    private ItemDetailsService itemDetaislService;
    @Mock
    private Promise<Map<Long, OfferDetail>> promiseOffer;

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
    	shoppingListServiceImp = new ShoppingListServiceImp(shoppingListDAO, offerStatusService, 
    			matchOfferService, itemDetailsService, offerCache, clubPriceDao, categoryCache, storeCache, emailBroker, storeDAO);
    }
    
    
    @Test
    public void testgetShoppingListForSuccess() throws Exception {
    	
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
    	
    	vo.setHeaderVO(header);
    	
    	List<Long> redeemedOfferList = Arrays.asList(new Long[] {500L,501L});
    	when(offerStatusService.findRedeemedOffersForRemoval(10001L)).thenReturn(redeemedOfferList);
    	
    	List<ShoppingListItem> shoppingListItems = new ArrayList<ShoppingListItem>();
    	ShoppingListItem item1 = new ShoppingListItem();
    	item1.setItemId("1001");
    	item1.setItemTypeCd("CC");
    	item1.setClipId("1001");
    	Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 15);
    	item1.setLastUpdTs(calendar.getTime());
    	item1.setStoreId(10000);
    	item1.setCategoryId(1);
    	shoppingListItems.add(item1);
    	
    	ShoppingListItem item2 = new ShoppingListItem();
    	item2.setItemId("1002");
    	item2.setItemTypeCd("YCS");
    	item2.setClipId("1002");
    	item2.setLastUpdTs(calendar.getTime());
    	item2.setStoreId(10000);
    	item2.setCategoryId(2);
    	shoppingListItems.add(item2);
    	
    	ShoppingListItem item3 = new ShoppingListItem();
    	item3.setItemId("1003");
    	item3.setItemTypeCd("MCS");
    	item3.setClipId("1003");
    	item3.setLastUpdTs(calendar.getTime());
    	item3.setStoreId(10000);
    	calendar.add(Calendar.DATE, 16);
    	item3.setItemEndDate(calendar.getTime());
    	item3.setCategoryId(3);
    	shoppingListItems.add(item3);
    	
    	ShoppingListItem item4 = new ShoppingListItem();
    	item4.setItemId("1004");
    	item4.setItemTypeCd("WS");
    	item4.setClipId("1004");
    	item4.setLastUpdTs(calendar.getTime());
    	item4.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item4.setItemEndDate(calendar.getTime());
    	item4.setCategoryId(4);
    	shoppingListItems.add(item4);
    	
    	ShoppingListItem item5 = new ShoppingListItem();
    	item5.setItemId("1004");
    	item5.setItemTypeCd("UPC");
    	item5.setClipId("1004");
    	item5.setLastUpdTs(calendar.getTime());
    	item5.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item5.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item5);

    	List<ShoppingListItemVO> listVO = new ArrayList<ShoppingListItemVO>();
    	ShoppingListItemVO itemVo = new ShoppingListItemVO();
    	itemVo.setCategoryId("1");
    	listVO.add(itemVo);
    	vo.setItems(listVO);
    	
    	vo.setUpdateYCSItem(shoppingListItems);
    	vo.setUpdateUPCItem(shoppingListItems);
    	
    	when(shoppingListDAO.getShoppingListItems(vo)).thenReturn(shoppingListItems);
    	
    	preferredStore = new PreferredStore();
    	preferredStore.setStoreId(10000);
    	when(storeDAO.findStoreInfo(any(Integer.class), any(Long.class), any(String.class))).thenReturn(preferredStore);
    	
    	/*when(detailsProviderMap.get(any())).thenReturn(itemDetailProvider);
    	when(itemDetailProvider.getItemDetails(any(Map.class), any(ShoppingListVO.class), any(Map.class))).thenReturn(listVO);*/
    	
    	Map<String, List<AllocatedOffer>> list = new HashMap<>();
    	AllocatedOffer offer = new AllocatedOffer();
    	offer.setClipId(1001L);
    	offer.setOfferId(5000L);
    	list.put("YCS", Arrays.asList(new AllocatedOffer[] { offer }));
    	list.put("CC", Arrays.asList(new AllocatedOffer[] { offer }));
    	
    	matchedOffers = new HashMap<>();
    	matchedOffers.put("100", list);
    	
    	when(matchOfferService.getRelatedOffers(any(), any())).thenReturn(matchedOffers);
    	
    	List<ShoppingListVO> returnShoppingListVOList = shoppingListServiceImp.getShoppingList(vo);
    	assertNotNull(returnShoppingListVOList);
    	assertEquals("MyListItems should match", vo.getHeaderVO(), returnShoppingListVOList.get(0).getHeaderVO());
    	
    	
    }
    
    @Test
    public void testgetShoppingListWithItemIDsForSuccess() throws Exception {
    	
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
    	vo.setHeaderVO(header);
    	
    	String [] itemids = {"1001","1002","1003","1004"};
    	vo.setItemIds(itemids);
    	
    	List<Long> redeemedOfferList = Arrays.asList(new Long[] {500L,501L});
    	when(offerStatusService.findRedeemedOffersForRemoval(10001L)).thenReturn(redeemedOfferList);
    	
    	List<ShoppingListItem> shoppingListItems = new ArrayList<ShoppingListItem>();
    	ShoppingListItem item1 = new ShoppingListItem();
    	item1.setItemId("1001");
    	item1.setItemTypeCd("CC");
    	item1.setClipId("1001");
    	Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 15);
    	item1.setLastUpdTs(calendar.getTime());
    	item1.setStoreId(10000);
    	shoppingListItems.add(item1);
    	
    	ShoppingListItem item2 = new ShoppingListItem();
    	item2.setItemId("1002");
    	item2.setItemTypeCd("YCS");
    	item2.setClipId("1002");
    	item2.setLastUpdTs(calendar.getTime());
    	item2.setStoreId(10000);
    	shoppingListItems.add(item2);
    	
    	ShoppingListItem item3 = new ShoppingListItem();
    	item3.setItemId("1003");
    	item3.setItemTypeCd("MCS");
    	item3.setClipId("1003");
    	item3.setLastUpdTs(calendar.getTime());
    	item3.setStoreId(10000);
    	calendar.add(Calendar.DATE, 16);
    	item3.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item3);
    	
    	ShoppingListItem item4 = new ShoppingListItem();
    	item4.setItemId("1004");
    	item4.setItemTypeCd("WS");
    	item4.setClipId("1004");
    	item4.setLastUpdTs(calendar.getTime());
    	item4.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item4.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item4);
    	
    	ShoppingListItem item5 = new ShoppingListItem();
    	item5.setItemId("1004");
    	item5.setItemTypeCd("UPC");
    	item5.setClipId("1004");
    	item5.setLastUpdTs(calendar.getTime());
    	item5.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item5.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item5);
    	
    	List<ShoppingListItemVO> listVO = new ArrayList<ShoppingListItemVO>();
    	ShoppingListItemVO itemVo = new ShoppingListItemVO();
    	itemVo.setCategoryId("1");
    	itemVo.setItemType("UPC");
    	listVO.add(itemVo);
    	vo.setItems(listVO);
    	
    	vo.setUpdateYCSItem(shoppingListItems);
    	vo.setUpdateUPCItem(shoppingListItems);

    	when(shoppingListDAO.getShoppingListItems(vo)).thenReturn(shoppingListItems);
    	
    	preferredStore = new PreferredStore();
    	preferredStore.setStoreId(10000);
    	when(storeDAO.findStoreInfo(any(Integer.class), any(Long.class), any(String.class))).thenReturn(preferredStore);
    	
    	/*when(detailsProviderMap.get(any())).thenReturn(itemDetailProvider);
    	when(itemDetailProvider.getItemDetails(any(Map.class), any(ShoppingListVO.class), any(Map.class))).thenReturn(listVO);
    	
    	when(asyncDetailProviderMap.get(any(Object.class))).thenReturn(itemDetailAsyncRetriever);
    	when(itemDetailAsyncRetriever.getAsyncDetails(any(String.class), any(Map.class), any(ShoppingListVO.class))).thenReturn(promise);*/
    	
    	Map<String, List<AllocatedOffer>> list = new HashMap<>();
    	AllocatedOffer offer = new AllocatedOffer();
    	offer.setClipId(1001L);
    	offer.setOfferId(5000L);
    	list.put("YCS", Arrays.asList(new AllocatedOffer[] { offer }));
    	list.put("CC", Arrays.asList(new AllocatedOffer[] { offer }));
    	
    	matchedOffers = new HashMap<>();
    	matchedOffers.put("100", list);
    	
    	when(matchOfferService.getRelatedOffers(any(), any())).thenReturn(matchedOffers);
    	
    	List<ShoppingListVO> returnShoppingListVOList = shoppingListServiceImp.getShoppingList(vo);
    	assertNotNull(returnShoppingListVOList);
    	assertEquals("MyListItems should match", vo.getHeaderVO(), returnShoppingListVOList.get(0).getHeaderVO());
    	
    	
    }
    
    @Test
    public void testgetShoppingListCountForSuccess() throws Exception {
    	
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
    	vo.setHeaderVO(header);
    	
    	String [] itemids = {"1001","1002","1003","1004"};
    	vo.setItemIds(itemids);
    	
    	List<Long> redeemedOfferList = Arrays.asList(new Long[] {500L,501L});
    	when(offerStatusService.findRedeemedOffersForRemoval(10001L)).thenReturn(redeemedOfferList);
    	
    	List<ShoppingListItem> shoppingListItems = new ArrayList<ShoppingListItem>();
    	ShoppingListItem item1 = new ShoppingListItem();
    	item1.setItemId("1001");
    	item1.setItemTypeCd("CC");
    	item1.setClipId("1001");
    	Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 15);
    	item1.setLastUpdTs(calendar.getTime());
    	item1.setStoreId(10000);
    	shoppingListItems.add(item1);
    	
    	ShoppingListItem item2 = new ShoppingListItem();
    	item2.setItemId("1002");
    	item2.setItemTypeCd("YCS");
    	item2.setClipId("1002");
    	item2.setLastUpdTs(calendar.getTime());
    	item2.setStoreId(10000);
    	shoppingListItems.add(item2);
    	
    	ShoppingListItem item3 = new ShoppingListItem();
    	item3.setItemId("1003");
    	item3.setItemTypeCd("MCS");
    	item3.setClipId("1003");
    	item3.setLastUpdTs(calendar.getTime());
    	item3.setStoreId(10000);
    	calendar.add(Calendar.DATE, 16);
    	item3.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item3);
    	
    	ShoppingListItem item4 = new ShoppingListItem();
    	item4.setItemId("1004");
    	item4.setItemTypeCd("WS");
    	item4.setClipId("1004");
    	item4.setLastUpdTs(calendar.getTime());
    	item4.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item4.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item4);
    	
    	ShoppingListItem item5 = new ShoppingListItem();
    	item5.setItemId("1004");
    	item5.setItemTypeCd("UPC");
    	item5.setClipId("1004");
    	item5.setLastUpdTs(calendar.getTime());
    	item5.setStoreId(10000);
    	calendar.add(Calendar.DATE, 17);
    	item5.setItemEndDate(calendar.getTime());
    	shoppingListItems.add(item5);
    	
    	List<ShoppingListItemVO> listVO = new ArrayList<ShoppingListItemVO>();
    	ShoppingListItemVO itemVo = new ShoppingListItemVO();
    	itemVo.setCategoryId("1");
    	itemVo.setItemType("CC");
    	itemVo.setTitle("(Default)");
    	listVO.add(itemVo);
    	vo.setItems(listVO);
    	
    	vo.setUpdateYCSItem(shoppingListItems);
    	vo.setUpdateUPCItem(shoppingListItems);

    	when(shoppingListDAO.getShoppingListItems(vo)).thenReturn(shoppingListItems);
    	
    	preferredStore = new PreferredStore();
    	preferredStore.setStoreId(10000);
    	when(storeDAO.findStoreInfo(any(Integer.class), any(Long.class), any(String.class))).thenReturn(preferredStore);
    	
    	Map<String, List<AllocatedOffer>> list = new HashMap<>();
    	AllocatedOffer offer = new AllocatedOffer();
    	offer.setClipId(1001L);
    	offer.setOfferId(5000L);
    	offer.setOfferId(5000L);
    	list.put("YCS", Arrays.asList(new AllocatedOffer[] { offer }));
    	list.put("CC", Arrays.asList(new AllocatedOffer[] { offer }));
    	
    	matchedOffers = new HashMap<>();
    	matchedOffers.put("100", list);
    	
    	when(matchOfferService.getRelatedOffers(any(), any())).thenReturn(matchedOffers);
    	
    	Integer count = shoppingListServiceImp.getShoppingListCount(vo,null);
    	assertNotNull(count);
    }
    
    
    @Test
    public void testgetShoppingListCountForApplicationException() throws Exception {
    	
    	ShoppingListVO vo = new ShoppingListVO();
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = "30/14/0001"; //Invalid timestamp
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("10001");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	vo.setHeaderVO(header);
    	
    	String [] itemids = {"1001","1002","1003","1004"};
    	vo.setItemIds(itemids);
    	
    	try {
    		shoppingListServiceImp.getShoppingListCount(vo,null);
            fail("Error Expected");
        }
        catch (ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
            assertEquals("Fault Code should match", FaultCodeBase.EMLS_INVALID_TIMESTAMP, e.getFaultCode());
        }
    	
    }
    
    @Test
    public void testsendShoppingListMailForApplicationException() throws Exception {
    	
    	PreferredStore pstore = new PreferredStore();
    	pstore.setStoreId(1000);
    	pstore.setRegionId(444);
    	
    	HeaderVO header = new HeaderVO();
    	header.setParamStoreId("10000");
    	header.setTimeZone("America/Los_Angeles");
        String sysTimestamp = DateHelper.getISODate(new Date(), header.getTimeZone());
    	header.setTimestamp(sysTimestamp);
    	header.setVersionValues(new boolean[] {true, true,true,true,true});
    	header.setSwyhouseholdid("10001");
    	header.setPostalcode("12345");
    	header.setDetails("y");
    	header.setPreferredStore(pstore);
    	
    	String [] emailids = {"puneet.srn@gmail.com"};
    	MailListVO mailListVO = new MailListVO();
    	mailListVO.setToEmails(emailids);
    	
    	List<String> sendList = Arrays.asList(new String[]{"1000","1001"});
    	ShoppingListGroup group = new ShoppingListGroup();
    	group.setItemIds(sendList);
    	ShoppingListGroup[] shoppingListGroup = {group};
    	mailListVO.setGroups(shoppingListGroup);

    	
    	when(storeDAO.findStoreInfo(any(Integer.class), any(Long.class),any(String.class))).thenReturn(pstore);
    	
    	
    	try {
    		shoppingListServiceImp.sendShoppingListMail(mailListVO, header);
            fail("Error Expected");
        }
        catch (ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
            assertEquals("Fault Code should match", FaultCodeBase.EMLS_NO_LIST_FOUND, e.getFaultCode());
        }
    }
}
