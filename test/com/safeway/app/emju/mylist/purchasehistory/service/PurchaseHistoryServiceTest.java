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

package com.safeway.app.emju.mylist.purchasehistory.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;


import java.io.File;
import java.time.temporal.ValueRange;
import java.util.ArrayList;
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

import com.safeway.app.emju.allocation.cliptracking.entity.MyListItemStatus;
import com.safeway.app.emju.allocation.cliptracking.model.OfferClipStatus;
import com.safeway.app.emju.allocation.cliptracking.service.OfferStatusService;
import com.safeway.app.emju.allocation.customerlookup.dao.CustomerLookupDAO;
import com.safeway.app.emju.allocation.dao.CCAllocationDAO;
import com.safeway.app.emju.allocation.dao.PDAllocationDAO;
import com.safeway.app.emju.allocation.dao.PurchasedItemDAO;
import com.safeway.app.emju.allocation.model.CCAllocatedOffer;
import com.safeway.app.emju.allocation.entity.PDCustomOffer;
import com.safeway.app.emju.allocation.entity.PurchasedItem;
import com.safeway.app.emju.allocation.exception.FaultCode;
import com.safeway.app.emju.allocation.exception.OfferServiceException;
import com.safeway.app.emju.allocation.helper.OfferConstants.ClipStatus;
import com.safeway.app.emju.allocation.helper.OfferConstants.ItemType;
import com.safeway.app.emju.allocation.helper.OfferConstants.OfferProgram;
import com.safeway.app.emju.allocation.partner.model.PartnerAllocationRequest;
import com.safeway.app.emju.allocation.partner.model.PartnerAllocationType;
import com.safeway.app.emju.allocation.partner.service.PartnerAllocationService;
import com.safeway.app.emju.allocation.pricing.dao.ClubPriceDAO;
import com.safeway.app.emju.allocation.pricing.dao.OfferStorePriceDAO;
import com.safeway.app.emju.allocation.pricing.entity.ClubPrice;
import com.safeway.app.emju.allocation.pricing.entity.OfferStorePrice;
import com.safeway.app.emju.allocation.requestidentification.model.ClientRequest;
import com.safeway.app.emju.cache.CacheAccessException;
import com.safeway.app.emju.cache.OfferDetailCache;
import com.safeway.app.emju.cache.RetailScanCache;
import com.safeway.app.emju.cache.StoreCache;
import com.safeway.app.emju.cache.dao.RetailScanOfferDAO;
import com.safeway.app.emju.cache.entity.OfferDetail;
import com.safeway.app.emju.cache.entity.RetailScanOffer;
import com.safeway.app.emju.cache.helper.ControlTableLocator;
import com.safeway.app.emju.dao.connector.CassandraConnector;
import com.safeway.app.emju.dao.exception.ConnectionException;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.exception.FaultCodeBase;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.model.AllocatedOffer;
import com.safeway.app.emju.mylist.purchasehistory.model.OfferHierarchy;
import com.safeway.app.emju.mylist.purchasehistory.model.json.PurchasedItemOffer;
import com.safeway.app.emju.mylist.purchasehistory.model.json.PurchasedItemOffers;
import com.safeway.app.emju.mylist.purchasehistory.parser.PurchaseHistoryRequest;
import com.safeway.app.emju.util.ListItemReference;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import play.Application;
import play.Configuration;
import play.libs.F.Promise;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.concurrent.ExecutionContext;

/**
 *
 * @author sshar64
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class PurchaseHistoryServiceTest {
	static {
        System.setProperty("config.file", "conf/mylist-local.conf");
        System.setProperty("logger.file", "test/logback-local.xml");
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(PurchaseHistoryServiceTest.class);

    private static Application fakeApp;

    @Mock
    private PurchasedItemDAO purchasedItemDAO;
    @Mock
    private PartnerAllocationService partnerAllocationService;
    @Mock
    private ClubPriceDAO clubPriceDAO;
    @Mock
    private OfferStatusService offerStatusService;
    @Mock
    private PDAllocationDAO pdAllocationDAO;
    @Mock
    private OfferStorePriceDAO offerStorePriceDAO;
    @Mock
    private CCAllocationDAO cCAllocationDAO;
    @Mock
    private RetailScanCache retailScanCache;
    @Mock
    private OfferDetailCache offerDetailCache;

    private static CassandraConnector cassandraConn;

    private RetailScanOfferDAO retailScanOfferDAO;



    @Mock private StoreCache storeCache;

    @Mock private CustomerLookupDAO customerLookupDAO;

    private PDOfferMapper pdOfferMapper;
    private CCOfferMapper ccOfferMapper;
    private YCSOfferMapper ycsOfferMapper;
    
    
    private PurchaseHistoryService service;
    private PurchaseHistoryRequest request;
    @Mock
    ExecutionContext daoContext;
    @Mock
    private Promise<List<Long>> partnerAllocationList;
    @Mock
    private PartnerAllocationRequest partnerAllocationRequest;
    @Mock
    Promise<Map<String, MyListItemStatus>> listStatusPromise;
    @Mock
    private ControlTableLocator controlTblLocator;
    

    @BeforeClass
    public static void start() {

        Config additionalConfig = ConfigFactory.parseFile(new File("test/purchase-test.conf"));
        Configuration additionalConfigurations = new Configuration(additionalConfig);

        fakeApp = Helpers.fakeApplication(additionalConfigurations.asMap());
        Helpers.start(fakeApp);
        LOGGER.debug("Initializing FaultCodes with PLAY system before mocking internal Play objects"
            + FaultCode.INVALID_CUSTOMER_GUID);
    }

    @AfterClass
    public static void stop() {
        Helpers.stop(fakeApp);
    }

    @Before
    public void setup() throws CacheAccessException, OfferServiceException {

        pdOfferMapper = new PDOfferMapper();
        ccOfferMapper = new CCOfferMapper();
        ycsOfferMapper = new YCSOfferMapper();



        service = new  PurchaseHistoryService( purchasedItemDAO
            ,  clubPriceDAO,
            offerStatusService,  pdAllocationDAO,
            offerStorePriceDAO,  cCAllocationDAO,
            retailScanCache,  offerDetailCache,
            ycsOfferMapper,  pdOfferMapper,  ccOfferMapper,
            partnerAllocationService,retailScanOfferDAO) ;


        /*OfferDetail offerDetail = new OfferDetail();
        offerDetail.setOfferId(603L);
        Calendar calendar = Calendar.getInstance();
    	calendar.add(Calendar.DATE, -1);
        offerDetail.setDisplayEffectiveStartDt(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        offerDetail.setDisplayEffectiveEndDt(calendar.getTime());
        calendar.add(Calendar.DATE, 1);
        offerDetail.setOfferEffectiveEndDt(calendar.getTime());
        offerDetail.setLastUpdateTs(calendar.getTime());
        offerDetail.setPrimaryCategoryId(123);
        offerDetail.setPriceMethodCd("123");
        
        
        OfferClipStatus offerClipStatus = new OfferClipStatus();
        offerClipStatus.setOfferId(603L);
        AllocatedOffer allocatedOffer = new AllocatedOffer();
        allocatedOffer.setOfferId(603L);*/

    }


    //@Test
    public void testFindPurchaseItemsAndOffersForSuccess() throws Exception {
    	
    	
    	Long householdId = 100L;
        Integer storeId = 1;
        Integer regionId = 123;
        List<Long> retailScanIds = Arrays.asList(201L, 202L, 203L);
        
    	// All the offers
        List<Long> offerIds = Arrays.asList(603L, 604L, 605L, 606L, 607L, 608L, 609L, 610L, 611L);

        // PD offers
        List<Long> pdOfferIds = Arrays.asList(603L,605L, 607L);

        // CC offers
        List<Long> ccOfferIds = Arrays.asList(603L,606L, 608L, 611L);
        List<Long> catalinaOfferIds = Arrays.asList(610L);

        // redeemed offers
        List<Long> redeemedOfferIds = Arrays.asList(604L, 609L);

        // clipped offers
        List<Long> clippedOfferIds = Arrays.asList(607L, 603L);

        // mylist items
        List<Long> myListRetailScanIds = Arrays.asList(203L);
    	
        request = new PurchaseHistoryRequest();

        request.setHouseholdId(householdId);
        request.setStoreId(storeId);
        request.setTimezone("America/Los_Angeles");
        request.setCustomerGUID("cutomer1");
        request.setRegionId(regionId);
        
    	Map<Long, PurchasedItem> purchasedItems = getPurchasedItems(householdId, retailScanIds);
        Map<Long, RetailScanOffer> retailScanOffers = getRetailScanOffers(retailScanIds);
        Map<Long, ClubPrice> clubPrices = getClubPrices(storeId, retailScanIds);
        Map<Long, PDCustomOffer> pdOffers = getPDOffers(householdId, pdOfferIds);
        Map<Long, OfferStorePrice> pdOfferStorePrices = getPDOfferStorePrices(storeId, pdOfferIds);
        Map<Long, OfferDetail> offerDetailsMap = buildOfferDetailMap(603L, 604L, 605L, 606L, 607L, 608L, 609L, 610L, 611L);

        Map<Long, CCAllocatedOffer> ccOffers = getCCOffers(ccOfferIds);
        Map<Long, OfferClipStatus> clippedOffers = getClippedOffers(clippedOfferIds);
        Map<String, MyListItemStatus> myListItemsStatus = getMyListItemsStatus(storeId, myListRetailScanIds);
        
    	when(purchasedItemDAO.findItemsByHousehold(any(Long.class))).thenReturn(purchasedItems);
        when(retailScanCache.getRetailScanOfferDetailByScanCds(anyVararg())).thenReturn(retailScanOffers);

        //YCS offers
        when(clubPriceDAO.findItemPrices(any(String.class), any(Integer.class), anyListOf(Long.class))).thenReturn(clubPrices);
        when(offerStatusService.findRedeemedOffersForRemoval(any(Long.class))).thenReturn(redeemedOfferIds);

        // PD offers
        when(pdAllocationDAO.findPDCustomAllocation(any(Long.class),any(Integer.class))).thenReturn(pdOffers);
        when(offerStorePriceDAO.findOfferPrices(any(Integer.class), anyListOf(Long.class))).thenReturn(pdOfferStorePrices);

        //CC offers, flag is for riq HTO call
        when(cCAllocationDAO.findCCAllocation(any(Integer.class))).thenReturn(ccOffers);
        when(partnerAllocationService.getAllocations(
            any(PartnerAllocationType.class), any(PartnerAllocationRequest.class))).thenReturn(catalinaOfferIds);

        //clipped offers
        when(offerStatusService.findOfferClipStatus(
            any(String.class), any(Long.class),anyListOf(Long.class), any(String[].class))).thenReturn(clippedOffers);

        //my list item status
        when(offerStatusService.findMyListItems(
            any(String.class), any(Long.class), any(Integer.class), any(String[].class))).thenReturn(myListItemsStatus);
        when(partnerAllocationService.getAsyncAllocations(any(PartnerAllocationType.class),any(PartnerAllocationRequest.class))).thenReturn(partnerAllocationList);
        when(offerStatusService.findMyListItemsAsync(any(ExecutionContext.class), any(String.class),any(Long.class),any(Integer.class), 
						any(String.class),any(String.class),any(String.class))).thenReturn(listStatusPromise);
        when(listStatusPromise.get(any(Long.class))).thenReturn(myListItemsStatus);
        when(offerDetailCache.getOfferDetailsByIds(anyVararg())).thenReturn(offerDetailsMap);

    	PurchasedItemOffers expected = preparePurchasedItemOffers();
        PurchasedItemOffers result = service.findPurchaseItemsAndOffers(request);

        assertNotNull(result);
        
        assertEquals(result.getCategories()[0].getCode(), expected.getCategories()[0].getCode());
        assertEquals(result.getCategories()[0].getName(), expected.getCategories()[0].getName());
        assertEquals(result.getCategories()[0].getCount(), expected.getCategories()[0].getCount());
    }

    //@Test
    public void testFindPurchaseItemsAndOffersForException() throws Exception {

        try {
        	service.findPurchaseItemsAndOffers(request);
            fail("Error Expected");
        }
        catch (ApplicationException e) {
            LOGGER.error(e.getMessage(), e);
            assertEquals("Fault Code should match", FaultCodeBase.UNEXPECTED_SYSTEM_FAILURE, e.getFaultCode());
        }
    }
    
    
    private PurchasedItemOffers preparePurchasedItemOffers() {

        PurchasedItemOffers purchasedItemOffers = new PurchasedItemOffers();

        List<PurchasedItemOffer> purchasedItemOfferList = new ArrayList<PurchasedItemOffer>();

        Map<Integer, OfferHierarchy> categoryMap = new HashMap<Integer, OfferHierarchy>(25);

        int count = 201;
        for(int i=0;i<3;i++) {

            PurchasedItemOffer purchasedItemOffer = new PurchasedItemOffer();

            purchasedItemOffer.setUpcId(new Long(count));
            purchasedItemOffer.setPurchaseCount(new Long(count));
            purchasedItemOffer.setTitleDsc1("Desc"+count);
            purchasedItemOffer.setProdDsc1("ProdDesc"+count);
            purchasedItemOffer.setCategoryId(new Long(i+count));
            purchasedItemOffer.setCategoryName("category"+count);
            purchasedItemOffer.setLastPurchasedTs(new Date());
            purchasedItemOffer.setItemType("UPC");

            purchasedItemOfferList.add(purchasedItemOffer);

            // Setup Category Hierarchy

            OfferHierarchy category = new OfferHierarchy();

            category.setCode(purchasedItemOffer.getCategoryId().toString());
            category.setName(purchasedItemOffer.getCategoryName());
            category.setCount(1L);

            categoryMap.put(purchasedItemOffer.getCategoryId().intValue(), category);

        }

        // convert list to array and set in PurchasedItemOffers object
        purchasedItemOffers.setItems(
            purchasedItemOfferList.toArray(new PurchasedItemOffer[purchasedItemOfferList.size()]));

        Collection<OfferHierarchy> categories = categoryMap.values();
        // convert collection to array and set in PurchasedItemOffers object
        purchasedItemOffers.setCategories(
            categories.toArray(new OfferHierarchy[categories.size()]));

        return purchasedItemOffers;

    }

    private Map<Long, RetailScanOffer> getRetailScanOffers(final List<Long> retailScanIds) {

        Map<Long, RetailScanOffer> retailScanOffers = new HashMap<Long, RetailScanOffer>(3);

        for(Long retailScanId : retailScanIds) {

            RetailScanOffer item = new RetailScanOffer();
            item.setRetailScanCd(retailScanId);
            item.setCategoryId(retailScanId.intValue());
            item.setCategoryNm("category"+retailScanId);
            item.setItemDesc("ItemDesc"+retailScanId);

            //For example retailScanId : 201
            // the offerIds are 603, 604, 605
            Long offerId = retailScanId * 3 ;
            item.setOfferIdList(
                Arrays.asList(offerId, offerId+1, offerId+2));

            retailScanOffers.put(retailScanId, item);
        }

        return retailScanOffers;

    }

    private Map<Long, ClubPrice> getClubPrices(final Integer storeId, final List<Long> retailScanIds) {

        Map<Long, ClubPrice> clubPrices = new HashMap<Long, ClubPrice>(3);

        for(Long retailScanId : retailScanIds) {

            ClubPrice item = new ClubPrice();
            item.setRetailScanCd(retailScanId);
            item.setCategoryId(retailScanId.intValue());
            item.setCategoryNm("category"+retailScanId);
            item.setStoreId(storeId);

            clubPrices.put(retailScanId, item);
        }

        return clubPrices;

    }

    private Map<Long, PurchasedItem> getPurchasedItems(final Long householdId, final List<Long> retailScanIds) {

        Map<Long, PurchasedItem> purchasedItems = new HashMap<Long, PurchasedItem>(3);

        for(Long retailScanId: retailScanIds) {
            PurchasedItem item = new PurchasedItem();
            item.setHouseholdId(householdId);
            item.setRetailScanCd(retailScanId);
            item.setPurchaseCnt(1);
            item.setPurchaseDt(new Date());
            purchasedItems.put(retailScanId, item);
        }

        return purchasedItems;

    }

    private Map<String, MyListItemStatus> getMyListItemsStatus(final Integer storeId, final List<Long> myListRetailScanIds) {

        Map<String, MyListItemStatus> myListItems = new HashMap<String, MyListItemStatus>(1);

        for(Long myListItemId: myListRetailScanIds) {
            MyListItemStatus item = new MyListItemStatus();

            item.setItemId(myListItemId.toString());

            String itemRefId = new ListItemReference(ItemType.YCS, myListItemId.toString(), storeId).getItemRefId();

            myListItems.put(itemRefId, item);
        }

        return myListItems;
    }

    private Map<Long, OfferClipStatus> getClippedOffers(final List<Long> clippedOfferIds) {

        Map<Long, OfferClipStatus> clippedOffers = new HashMap<Long, OfferClipStatus>(2);

        for(Long clippedOfferId: clippedOfferIds) {
            OfferClipStatus item = new OfferClipStatus();
            item.setOfferId(clippedOfferId);
            item.setClipStatus(ClipStatus.ADDED_TO_CARD);
            clippedOffers.put(clippedOfferId, item);
        }

        return clippedOffers;
    }

    private Map<Long, CCAllocatedOffer> getCCOffers(final List<Long> ccOfferIds) {

        Map<Long, CCAllocatedOffer> ccOfferPrices = new HashMap<Long, CCAllocatedOffer>(4);

        for(Long ccOfferId: ccOfferIds) {
            CCAllocatedOffer item = new CCAllocatedOffer();
            item.setStoreId(4566);
            item.setOfferId(ccOfferId);
            ccOfferPrices.put(ccOfferId, item);
        }

        return ccOfferPrices;
    }

    private Map<Long, OfferStorePrice> getPDOfferStorePrices(final Integer storeId, final List<Long> pdOfferIds) {

        Map<Long, OfferStorePrice> pdOfferPrices = new HashMap<Long, OfferStorePrice>(2);

        for(Long pdOfferId: pdOfferIds) {
            OfferStorePrice item = new OfferStorePrice();
            item.setStoreId(storeId);
            item.setRegularPrice(100d);
            item.setOfferId(pdOfferId);
            pdOfferPrices.put(pdOfferId, item);
        }

        return pdOfferPrices;
    }

    private Map<Long, PDCustomOffer> getPDOffers(final Long householdId, final List<Long> pdOfferIds) {

        Map<Long, PDCustomOffer> pdOffers = new HashMap<Long, PDCustomOffer>(2);

        for(Long pdOfferId: pdOfferIds) {

            PDCustomOffer item = new PDCustomOffer();
            item.setHouseholdId(householdId);
            item.setOfferId(pdOfferId);
            item.setRank(1L);
            item.setRegionId(1);
            item.setPreviouslyPurchased(1);

            pdOffers.put(pdOfferId, item);
        }

        return pdOffers;
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
    
    @Test
    public void testFindPurchaseItemsAndOffersForSuccessNAI() throws Exception {
    	
    	System.out.println("testFindPurchaseItemsAndOffersForSuccessNAI");
    	Long householdId = 100L;
        Integer storeId = 1;
        Integer regionId = 123;
        List<Long> retailScanIds = Arrays.asList(201L, 202L, 203L);
        
    	// All the offers
        List<Long> offerIds = Arrays.asList(603L, 604L, 605L, 606L, 607L, 608L, 609L, 610L, 611L);

        // PD offers
        List<Long> pdOfferIds = Arrays.asList(603L,605L, 607L);

        // CC offers
        List<Long> ccOfferIds = Arrays.asList(603L,606L, 608L, 611L);
        List<Long> catalinaOfferIds = Arrays.asList(610L);

        // redeemed offers
        List<Long> redeemedOfferIds = Arrays.asList(604L, 609L);

        // clipped offers
        List<Long> clippedOfferIds = Arrays.asList(607L, 603L);

        // mylist items
        List<Long> myListRetailScanIds = Arrays.asList(203L);
    	
        request = new PurchaseHistoryRequest();

        request.setHouseholdId(householdId);
        request.setStoreId(storeId);
        request.setTimezone("America/Los_Angeles");
        request.setCustomerGUID("cutomer1");
        request.setRegionId(regionId);
        //request.setBanner("Shaws");
        request.setBanner("safeway");
        
    	Map<Long, PurchasedItem> purchasedItems = getPurchasedItems(householdId, retailScanIds);
        Map<Long, RetailScanOffer> retailScanOffers = getRetailScanOffers(retailScanIds);
        Map<Long, ClubPrice> clubPrices = getClubPrices(storeId, retailScanIds);
        Map<Long, PDCustomOffer> pdOffers = getPDOffers(householdId, pdOfferIds);
        Map<Long, OfferStorePrice> pdOfferStorePrices = getPDOfferStorePrices(storeId, pdOfferIds);
        Map<Long, OfferDetail> offerDetailsMap = buildOfferDetailMap2(603L, 604L, 605L, 606L, 607L, 608L, 609L, 610L, 611L);

        Map<Long, CCAllocatedOffer> ccOffers = getCCOffers(ccOfferIds);
        Map<Long, OfferClipStatus> clippedOffers = getClippedOffers(clippedOfferIds);
        Map<String, MyListItemStatus> myListItemsStatus = getMyListItemsStatus(storeId, myListRetailScanIds);
        
    	when(purchasedItemDAO.findItemsByHousehold(any(Long.class))).thenReturn(purchasedItems);
        when(retailScanCache.getRetailScanOfferDetailByScanCds(anyVararg())).thenReturn(retailScanOffers);

        //YCS offers
        when(clubPriceDAO.findItemPrices(any(String.class), any(Integer.class), anyListOf(Long.class))).thenReturn(clubPrices);
        when(offerStatusService.findRedeemedOffersForRemoval(any(Long.class))).thenReturn(redeemedOfferIds);

        // PD offers
        when(pdAllocationDAO.findPDCustomAllocation(any(Long.class),any(Integer.class))).thenReturn(pdOffers);
        when(offerStorePriceDAO.findOfferPrices(any(Integer.class), anyListOf(Long.class))).thenReturn(pdOfferStorePrices);

        //CC offers, flag is for riq HTO call
        when(cCAllocationDAO.findCCAllocation(any(Integer.class))).thenReturn(ccOffers);
        when(partnerAllocationService.getAllocations(
            any(PartnerAllocationType.class), any(PartnerAllocationRequest.class))).thenReturn(catalinaOfferIds);

        //clipped offers
        when(offerStatusService.findOfferClipStatus(
            any(String.class), any(Long.class),anyListOf(Long.class), any(String[].class))).thenReturn(clippedOffers);

        //my list item status
        when(offerStatusService.findMyListItems(
            any(String.class), any(Long.class), any(Integer.class), any(String[].class))).thenReturn(myListItemsStatus);
        when(partnerAllocationService.getAsyncAllocations(any(PartnerAllocationType.class),any(PartnerAllocationRequest.class))).thenReturn(partnerAllocationList);
        when(offerStatusService.findMyListItemsAsync(any(ExecutionContext.class), any(String.class),any(Long.class),any(Integer.class), 
						any(String.class),any(String.class),any(String.class))).thenReturn(listStatusPromise);
        when(listStatusPromise.get(any(Long.class))).thenReturn(myListItemsStatus);
        when(offerDetailCache.getOfferDetailsByIds(anyVararg())).thenReturn(offerDetailsMap);

    	PurchasedItemOffers expected = preparePurchasedItemOffers();
        PurchasedItemOffers result = service.findPurchaseItemsAndOffers(request);

        assertNotNull(result);
        PurchasedItemOffer[] purchasedItemOfferArray= result.getItems();
        
        for(int i=0; i<purchasedItemOfferArray.length; i++){
        		AllocatedOffer[] allocatedOfferArray = purchasedItemOfferArray[i].getRelatedOffers();
        		if(allocatedOfferArray!=null) {
        			for(int j=0; j<allocatedOfferArray.length; j++){
        				System.out.println("OfferPgm="+ allocatedOfferArray[j].getOfferPgm());
        			}
        		}
        }
        
        assertEquals(result.getCategories()[0].getCode(), expected.getCategories()[0].getCode());
        assertEquals(result.getCategories()[0].getName(), expected.getCategories()[0].getName());
        assertEquals(result.getCategories()[0].getCount(), expected.getCategories()[0].getCount());
    }
    
    private Map<Long, OfferDetail> buildOfferDetailMap2(final Long... OfferIDs) {

    	Map<Long, OfferDetail> listOffer = new HashMap<Long, OfferDetail>();
    	OfferDetail offer = null;
    int count=0;
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
        	if(count%3==0)
        		offer.setServiceProviderNm("COUPONSINCNAI");
        	else if(count%3==1)
        		offer.setServiceProviderNm("COUPONSINCSCNAI");
        	else if(count%3==2)
        		offer.setServiceProviderNm("COUPONSINC");
        	count++;
        	listOffer.put(offerId, offer);
        }

        return listOffer;
    }
}

