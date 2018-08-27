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

package com.safeway.app.emju.mylist.model;


import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
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
import com.safeway.app.emju.exception.FaultCodeBase;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mylist.dao.ShoppingListDAO;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.helper.DateHelper;
import com.safeway.app.emju.mylist.model.HeaderVO;
import com.safeway.app.emju.mylist.model.PreferredStore;
import com.safeway.app.emju.mylist.model.ShoppingListItemVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.service.detail.GRItemDetailAsyncRetriever;
import com.safeway.emju.redis.RedisCacheManager;
import com.safeway.app.emju.mylist.constant.OfferConstants;

import play.Application;
import play.Configuration;
import play.Play;
import play.libs.Akka;
import play.test.Helpers;
import scala.concurrent.ExecutionContext;

/* ***************************************************************************
 * NAME         : AllModelTest.java
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
public class AllModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AllModelTest.class);
    private static Application fakeApp;
    
    private static String STRING_CONST = "qtETgwdgRYhdDyH";
    private static int INT_CONST = 123456789;
    private static Integer INTEGER_CONST = new Integer(123456789);
    private static Long LONG_CONST = new Long(1);
    private static Date DATE_CONST = new Date(0);
    
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

    }
    
    
    @Test
    public void testAllModel() throws Exception {
    	
    	AllocatedOfferDetail offerDetail = new AllocatedOfferDetail();
    	offerDetail.toString();
    	
    	CategoryHierarchyVO catHierVo = new CategoryHierarchyVO();
    	catHierVo.toString();
    	ErrorVO errVo = new  ErrorVO("INVALID_INPUT_DATA", "Input data is NOT valid.");
    	errVo.toString();
    	GroceryRewardsVO groceryVO = new GroceryRewardsVO();
    	groceryVO.toString();
    	HeaderVO header = new HeaderVO();
    	header.toString();
    	MailListVO mailVO = new MailListVO();
    	mailVO.toString();
    	
    }
    
    @Test
    public void testAllocatedOffer() throws Exception {
    	AllocatedOffer offer = new AllocatedOffer();
    	offer.setCategoryName(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getCategoryName());
    	offer.setClipId(LONG_CONST);
    	assertEquals(LONG_CONST.longValue(), offer.getClipId().longValue());
    	offer.setClipId(0L);
    	assertEquals(0L, offer.getClipId().longValue());
    	offer.setClipStatus(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getClipStatus());
    	offer.setDefaultCategoryRank(INT_CONST);
    	assertEquals(INT_CONST, offer.getDefaultCategoryRank());
    	offer.setOfferPgm(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getOfferPgm());
    	offer.setOfferSubPgm(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getOfferSubPgm());
    	offer.setDefaultAllocation(true);
    	
    	offer.setDeleteTs(DATE_CONST);
    	assertEquals(DATE_CONST, offer.getDeleteTs());
    	offer.setRewardsRequired(INTEGER_CONST);
    	assertEquals(INTEGER_CONST, offer.getRewardsRequired());
    	offer.setListStatus(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getListStatus());
    	offer.setOfferProgramTypeCd(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getOfferProgramTypeCd());
    	offer.setOfferProvider(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getOfferProvider());
    	offer.setOfferStatus(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getOfferStatus());
    	offer.setTitleDescription(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getTitleDescription());
    	offer.setPurchaseHistOfferId(LONG_CONST);
    	assertEquals(LONG_CONST, offer.getPurchaseHistOfferId());
    	
    	offer.setPurchaseHistRank(INT_CONST);
    	assertEquals(INT_CONST, offer.getPurchaseHistRank());
    	offer.setHhOfferRank(INTEGER_CONST);
    	assertEquals(INTEGER_CONST, offer.getHhOfferRank());
    	
    	offer.setHhPurchaseCategoryRank(INT_CONST);
    	assertEquals(INT_CONST, offer.getHhPurchaseCategoryRank());
    	offer.setHhOfferRank(INTEGER_CONST);
    	assertEquals(INTEGER_CONST, offer.getHhOfferRank());
    	
    	offer.setImageId(STRING_CONST);
    	assertEquals(STRING_CONST, offer.getImageId());
    	
    	// Comparison tests
    	assertTrue(offer.equals(offer));
    	assertFalse(offer.equals(null));
    	assertFalse(offer.equals(new AllocatedOfferDetail()));
    	
    	AllocatedOffer offer123 = new AllocatedOffer();
    	offer123.setOfferId(123L);
    	AllocatedOffer offer456 = new AllocatedOffer();
    	offer456.setOfferId(456L);
    	AllocatedOffer offer123Clone = new AllocatedOffer();
    	offer123Clone.setOfferId(123L);
    	
    	assertTrue(offer123.equals(offer123Clone));
    	assertFalse(offer456.equals(offer123));
    	assertFalse(offer456.equals(offer)); // offer has no offerId set
    	
    	offer.toString();

    }
    
    @Test
    public void testOfferDetailMapper() throws Exception {
    	
    	Map<String, Object> detail = new HashMap<String, Object>();
    	Map<String, Object> nullDetail = null;
    	
    	OfferDetailMapper offerDetailMapper = new OfferDetailMapper();
    	nullDetail = offerDetailMapper.getDetail();
    	assertNull(nullDetail);
    	
    	offerDetailMapper = new OfferDetailMapper(detail);
    	detail = offerDetailMapper.getDetail();
    	assertEquals(true, detail.isEmpty());
    	
    	offerDetailMapper.setArrivalRank(INT_CONST);
    	offerDetailMapper.setCategoryNm(STRING_CONST);
    	offerDetailMapper.setCategoryRank(INT_CONST);
    	offerDetailMapper.setCompetitorName(STRING_CONST);
    	offerDetailMapper.setCompetitorPriceAmount(STRING_CONST);
    	offerDetailMapper.setDefaultPriceZoneOfferRankNbr(INT_CONST);
    	offerDetailMapper.setDisplayRank(INT_CONST);
    	offerDetailMapper.setExpiryRank(INT_CONST);
    	offerDetailMapper.setHouseholdOfferRankNbr(INT_CONST);
    	offerDetailMapper.setOfferEndDt(DATE_CONST);
    	offerDetailMapper.setOfferPrice(STRING_CONST);
    	offerDetailMapper.setOfferRankNbr(INT_CONST);
    	offerDetailMapper.setOfferStartDt(DATE_CONST);
    	offerDetailMapper.setPreviouslyPurchaseInd(STRING_CONST);
    	offerDetailMapper.setPriceMethodSubType(STRING_CONST);
    	offerDetailMapper.setPriceMethodType(STRING_CONST);
    	offerDetailMapper.setPriceTitle1(STRING_CONST);
    	offerDetailMapper.setPriceTitle2(STRING_CONST);
    	offerDetailMapper.setPriceTitle2Type(STRING_CONST);
    	offerDetailMapper.setPriceValue1(STRING_CONST);
    	offerDetailMapper.setPriceValue2(STRING_CONST);
    	offerDetailMapper.setProdDsc1(STRING_CONST);
    	offerDetailMapper.setProdDsc2(STRING_CONST);
    	offerDetailMapper.setPurchaseCountValue(STRING_CONST);
    	offerDetailMapper.setPurchaseRank(INT_CONST);
    	offerDetailMapper.setRedeemCount(INT_CONST);
    	offerDetailMapper.setRedeemDate(DATE_CONST);
    	offerDetailMapper.setRegularRetailPriceAmount(STRING_CONST);
    	offerDetailMapper.setSavingsPct(STRING_CONST);
    	offerDetailMapper.setSavingsValue(STRING_CONST);
    	offerDetailMapper.setStoreId(INT_CONST);
    	offerDetailMapper.setStoreName(STRING_CONST);
    	offerDetailMapper.setTitleDsc1(STRING_CONST);
    	offerDetailMapper.setTitleDsc2(STRING_CONST);

    	detail = offerDetailMapper.getDetail();
    	assertEquals(INT_CONST, detail.get(OfferConstants.OfferDetail.ARRIVAL_RANK));
    	assertEquals(STRING_CONST, detail.get(OfferConstants.OfferDetail.CATEGORY_NM));
    	assertEquals(INT_CONST, detail.get(OfferConstants.OfferDetail.CATEGORY_RANK));
    	assertEquals(STRING_CONST, detail.get(OfferConstants.OfferDetail.COMPETITOR_NM));
    	assertEquals(STRING_CONST, detail.get(OfferConstants.OfferDetail.COMPETITOR_PRICE_AMT));
    	assertEquals(INT_CONST, detail.get(OfferConstants.OfferDetail.RANK_NBR));
    	
    }
    
}
