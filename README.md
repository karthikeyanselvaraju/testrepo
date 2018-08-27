# emju-mylist test different subscriptions
Code repository for mylist features



ShoppingListController <br />
—ShoppingListServiceImp->getShoppingList <br />
——setPreferredStoreInfo <br />
————findPrimaryStore(householdID) <br />
———————controlTableLocator.findByControlTableId(ControlTableLocator.HOUSEHOLD_STORE) <br />
———————select store_id from table where household_id = ? <br />
————findStoreDetails(userStoreId, registeredZipCode)<br />
——————— SELECT store_id, price_zone_id, banner_nm, address_line_1, address_line_2, city, state, country, postal_cd, region_id,<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rog_cd, division_nm, capture_dt, timezone_nm FROM emju.store WHERE store_id = :storeId")<br />
——validateStoreInRequest<br />
————storeCache.getStoreDetailsById(Integer.parseInt(storeId))<br />
———————SELECT store_id, price_zone_id, banner_nm, address_line_1, address_line_2, city, state, country, postal_cd, region_id,<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; rog_cd, division_nm, capture_dt, timezone_nm FROM emju.store WHERE store_id = :storeId<br />
——offerStatusService.findRedeemedOffersForRemoval(houseHoldId)<br />
————-"SELECT offer_id FROM emju.household_redeemed_offer_nodisplay WHERE household_id = :householdId"<br />
——shoppingListDAO.getShoppingListItems(shoppingListVO)<br />
————-"SELECT retail_customer_id, household_id, shopping_list_nm, item_type_cd, store_id, " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "item_id, category_id, checked_ind, clip_id, clip_ts, club_card_nbr, delete_ts, " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "item_desc, item_end_dt, item_image, item_price_value, item_promo_price, item_quantity, " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "item_ref_id, item_start_dt, item_title, last_upd_ts, last_upd_usr_id, &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; shopping_list_desc, " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "source_application_cd, ttl(clip_id) FROM emju.mylist_items " + <br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "WHERE retail_customer_id = ? AND household_id = ? AND shopping_list_nm = ?";<br />
——setItemDetails<br />
————matchOfferService.getRelatedOffers(upcItemMap, shoppingListVO.getHeaderVO())<br />
———————getPartnerAllocationOffers<br />
————————————partnerAllocationDAO.findCCPartnerAllocation(householdId, householdSessionId, allocationDt)<br />
———————————————“SELECT offer_id FROM emju.cc_hh_realtime_partner_allocation WHERE household_id = :householdId " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "AND household_session_id = :householdSessionId AND allocation_dt = :allocationDt"<br />
————————————getCatalinaAllocationsAsync(threadCtx, householdId, cardNbr)<br />
———————————————serviceClient.getCatalinaAllocatedOffers(householdId, cardNbr)         (WebService Call)<br />
———————————————offerCache.getOfferDetailsByCopientIds(externalOfferIds, ServiceProvider.CATALINA)<br />
——————————————————redisManager.mget<br />
——————————————————SELECT * FROM emju.copient_offer WHERE external_offer_id IN ?<br />
——————————————————redisManager.set<br />
————————————getRiQAllocationsAsync(threadCtx, householdId)<br />
———————————————serviceClient.getRiQAllocatedOffers(householdId)<br />
———————————————offerCache.getOfferDetailsByAggregatorId(aggregatorOfferId, ServiceProvider.COUPONSINC)<br />
————————————getCouponsIncAllocationsAsync(threadCtx, householdId, cardNbr)<br />
———————————————serviceClient.getCouponsIncAllocatedOffers(householdId, cardNbr)<br />
———————————————offerCache.getOfferDetailsByAggregatorId(aggregatorOfferId, ServiceProvider.COUPONSINC)<br />
————————————cacheCCVendorAllocation(vendorOffers)<br />
———————————————vendorAllocation.forEach((final CCPartnerOffer o) -> { batch.add(mapper.saveQuery(o, TIME_TO_LIVE)); });<br />
———————getYCSOffers<br />
————————————purchasedItemDAO.findItemsByHHIdAndScanCode(householdId, upcList)<br />
———————————————controlTblLocator.findByControlTableId(ControlTableLocator.PURCHASE_HISTORY)<br />
———————————————select * from table where household_id=? and retail_scan_cd in ?<br />
————————————clubPriceDao.findItemPrices(timeZone, storeId, retailScanCds)<br />
———————————————controlTableLocator.findByControlTableId(ControlTableLocator.STORE_CLUB_PRICE)<br />
———————————————select from table where store_id = ? and retail_scan_cd= ? and effective_start_dt <= ?<br />
———————retailScanCache.getRetailScanOfferDetailByScanCds(array)<br />
————————————redisCacheManager.mget(strKeys)<br />
————————————rtlScanOfferDAO.getRetailScanOfferDetailByScanCdsAsync(cacheMissRetailScanCds)<br />
———————————————controlTableLocator.findByControlTableId(ControlTableLocator.RETAIL_SCAN_OFFER)<br />
———————————————"select * from emju.").append(table).append(" where retail_scan_cd = ?;"<br />
————————————redisCacheManager.set(rKey, rtlScanOfferJson)<br />
———————offerStatusService.findRedeemedOffersForRemoval(householdId)<br />
————————————"SELECT offer_id FROM emju.household_redeemed_offer_nodisplay WHERE household_id = :householdId"<br />
———————offerStatusService.findOfferClipStatus(headerVO.getSwycustguid(), householdId,relatedOfferIds, null)<br />
————————————offerStatusDAO.findMyCardOffersById(customerGUID, householdId, offerTypes, offerIds)<br />
———————————————"SELECT offer_id, customer_friendly_program_id, clip_id, offer_clip_ts FROM emju.clipped_offer " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "WHERE retail_customer_id = :customerGUID AND household_id = :householdId " +<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "AND customer_friendly_program_id = :offerPgm AND offer_id IN :offerIds"<br />
————————————offerStatusDAO.findMyListItems(customerGUID, householdId)<br />
———————————————SELECT item_ref_id, item_id, item_type_cd, delete_ts FROM emju.mylist_items <br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; WHERE retail_customer_id = :customerGUID AND household_id = :householdId<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; AND shopping_list_nm = :defaultListNm<br />
———————getPDOffers<br />
————————————pdAllocationDAO.findPDCustomAllocation(householdId, regionId,offerList)<br />
———————————————controlTblLocator.findByControlTableId(ControlTableLocator.PD_CUSTOM_HOUSEHOLD_OFFER)<br />
———————————————select * from table where household_id = ? and region_id = ? and offer_id in ?<br />
————————————offerCache.getOfferDetailsByIds(array)<br />
———————————————redisManager.mget(redisKeysArray)<br />
———————————————select * from emju.offer_detail where offer_id = ?<br />
————————————pricingDAO.findOfferPrices(storeId, validPDOfferIds)<br />
———————————————controlTblLocator.findByControlTableId(ControlTableLocator.OFFER_STORE_PRICE)<br />
———————————————QueryBuilder.select().from(table).where(eq("store_id", bindMarker())).and(in("offer_id", bindMarker()))<br />
———————getCCOffers<br />
————————————ccAllocationDAO.findCCAllocation(zipCode, offerList)<br />
———————————————SELECT * FROM emju.postal_cc_allocation WHERE postal_cd = :postalCd AND offer_id IN :offerIds<br />
————————————offerCache.getOfferDetailsByIds(array)<br />
———————————————redisManager.mget(redisKeysArray)<br />
———————————————select * from emju.offer_detail where offer_id = ?<br />
————itemDetaislService.getAsyncDetails(itemType, itemMap, shoppingListVO)<br />
———————UPCItemDetailAsyncRetriever<br />
———————————redisCacheManager.mget(strKeys)<br />
———————————controlTableLocator.findByControlTableId(ControlTableLocator.RETAIL_SCAN_OFFER)<br />
———————————select * from emju.table where retail_scan_cd = ?<br />
———————CCItemDetailAsyncRetriever<br />
———————————ccAllocationDAO.findCCAllocation(postalCode)<br />
———————————————select * from postal_cc_allocation  where postal_cd=?<br />
———————————offerCache.getOfferDetailsByIds(validOfferIdsArray)<br />
———————————————redisManager.mget(redisKeysArray)<br />
———————————————select * from emju.offer_detail where offer_id = ?<br />
———————PDItemDetailAsyncRetriever<br />
———————————pdAllocationDAO.findPDCustomAllocation(Long.valueOf(hhId), regionId)<br />
———————————————controlTblLocator.findByControlTableId(ControlTableLocator.PD_CUSTOM_HOUSEHOLD_OFFER);<br />
———————————————select * from  table  where household_id = ? and region_id = ?<br />
———————————pdAllocationDAO.findPDDefaultAllocation(storeId)<br />
———————————————SELECT * FROM emju.pd_default_offer WHERE store_id = :storeId<br />
———————————offerCache.getOfferDetailsByIds(validOfferIdsArray)<br />
———————————————redisManager.mget(redisKeysArray)<br />
———————————————select * from emju.offer_detail where offer_id = ?<br />
———————YCSItemDetailAsyncRetriever<br />
———————————controlTableLocator.findByControlTableId(ControlTableLocator.STORE_CLUB_PRICE)<br />
———————————select from table where store_id=?<br />
———————WSItemDetailAsyncRetriever<br />
———————————redisManager.mget(strKeys)<br />
———————————weeklyAddDAO.getWeeklyAddByOfferId(cacheMissedOffers)<br />
———————————————SELECT * FROM emju.offer_weekly_ad WHERE offer_id IN ?<br />
———————————redisManager.setString(sKey, jsonValue, duration)<br />
————itemDetaislService.setItemDetails(itemType, itemMap, shoppingListVO, matchedOffers)<br />
————itemDetaislService.setItemDetails(entry.getKey(), itemDetail, itemMap, shoppingListVO)<br />
——setUpdatableTTLItems<br />
————mapper.saveQuery(item, Option.ttl(item.getTtl()))<br />
——setCategoryDetails<br />
————if(ValidationHelper.isEmpty(categoryMap))<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; redisManager.set(CATEGORY_CACHE_KEY_PREFIX …<br />
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; redisManager.get(CATEGORY_CACHE_KEY_PREFIX …<br />
<br />
<br />
<br />
—                   Level 1<br />
——                  Level 2<br />
————                Level 3<br />
———————             Level 4<br />
————————————        Level 5<br />
———————————————     Level 6<br />
——————————————————  Level 7<br />

