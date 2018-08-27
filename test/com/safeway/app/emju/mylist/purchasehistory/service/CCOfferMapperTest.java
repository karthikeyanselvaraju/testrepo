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
import java.sql.Timestamp;
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
import com.safeway.app.emju.allocation.helper.OfferConstants.PurchaseIndicator;
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
import com.safeway.app.emju.helper.DataHelper;
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

import play.Configuration;
import play.libs.F.Promise;
import play.test.FakeApplication;
import play.test.Helpers;
import scala.concurrent.ExecutionContext;

/**
 *
 * @author psaxe00
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class CCOfferMapperTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(CCOfferMapperTest.class);

    private static FakeApplication fakeApp;
    private CCOfferMapper mapper;

    

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
    		
    	mapper = new CCOfferMapper();

    }


    @Test
    public void testmapOfferForSuccess() throws Exception {
    	
    	OfferDetail offerDetail = new OfferDetail();
    	offerDetail.setOfferId(10000L);
    	offerDetail.setOfferStatusTypeId("O");
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

    	List<Long> catalinaAllocationOffers = Arrays.asList(10000L);
    	
    	OfferClipStatus offerClipStatus = new OfferClipStatus();
    	offerClipStatus.setClipStatus("I");
    	offerClipStatus.setOfferId(50000L);
    	
    	String timezone = "America/Los_Angeles";
    	
    	AllocatedOffer expectedOffer = new AllocatedOffer();
    	expectedOffer.setOfferId(offerDetail.getOfferId());
    	expectedOffer.setOfferPgm(offerDetail.getOfferProgramCd());
    	expectedOffer.setOfferTs(new Timestamp(offerDetail.getLastUpdateTs().getTime()));
    	expectedOffer.setOfferProgramTypeCd(offerDetail.getOfferSubProgram());
    	expectedOffer.setShoppingListCategoryId(offerDetail.getPrimaryCategoryId());
    	expectedOffer.setPriceMethodCd(DataHelper.replaceIfNull(offerDetail.getPriceMethodCd()));
    	expectedOffer.setExtlOfferId(offerDetail.getExternalOfferId());
    	expectedOffer.setClipStatus( offerClipStatus.getClipStatus());
    	expectedOffer.setClipId(offerClipStatus.getOfferId());
    	expectedOffer.setPurchaseInd(PurchaseIndicator.PURCHASE_IND_BOUGHT); 
    	expectedOffer.getOfferDetail().setStartDt(offerDetail.getDisplayEffectiveStartDt());
    	expectedOffer.getOfferDetail().setEndDt(offerDetail.getOfferEffectiveEndDt());
    	
    	AllocatedOffer actual = mapper.mapOffer(offerDetail, catalinaAllocationOffers, offerClipStatus, timezone);
    	assertNotNull(actual);
    	assertEquals(expectedOffer, actual);
    	
    }

    @Test
    public void testmapOfferForUnSuccess() throws Exception {
    	
    	OfferDetail offerDetail = new OfferDetail();
    	offerDetail.setOfferId(10000L);
    	offerDetail.setOfferStatusTypeId("O");
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

    	List<Long> catalinaAllocationOffers = Arrays.asList(10000L);
    	
    	OfferClipStatus offerClipStatus = new OfferClipStatus();
    	offerClipStatus.setClipStatus("U");
    	offerClipStatus.setOfferId(50000L);
    	
    	String timezone = "America/Los_Angeles";
    	
    	AllocatedOffer expectedOffer = new AllocatedOffer();
    	expectedOffer.setOfferId(offerDetail.getOfferId());
    	expectedOffer.setOfferPgm(offerDetail.getOfferProgramCd());
    	expectedOffer.setOfferTs(new Timestamp(offerDetail.getLastUpdateTs().getTime()));
    	expectedOffer.setOfferProgramTypeCd(offerDetail.getOfferSubProgram());
    	expectedOffer.setShoppingListCategoryId(offerDetail.getPrimaryCategoryId());
    	expectedOffer.setPriceMethodCd(DataHelper.replaceIfNull(offerDetail.getPriceMethodCd()));
    	expectedOffer.setExtlOfferId(offerDetail.getExternalOfferId());
    	expectedOffer.setClipStatus( offerClipStatus.getClipStatus());
    	expectedOffer.setClipId(offerClipStatus.getOfferId());
    	expectedOffer.setPurchaseInd(PurchaseIndicator.PURCHASE_IND_BOUGHT); 
    	expectedOffer.getOfferDetail().setStartDt(offerDetail.getDisplayEffectiveStartDt());
    	expectedOffer.getOfferDetail().setEndDt(offerDetail.getOfferEffectiveEndDt());
    	
    	AllocatedOffer actual = mapper.mapOffer(offerDetail, catalinaAllocationOffers, offerClipStatus, timezone);
    	assertEquals(null, actual);
    	
    }
}

