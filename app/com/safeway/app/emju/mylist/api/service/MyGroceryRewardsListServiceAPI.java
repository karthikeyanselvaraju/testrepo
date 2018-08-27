package com.safeway.app.emju.mylist.api.service;

import com.google.inject.ImplementedBy;
import com.safeway.app.emju.mobile.exception.MobileException;
import com.safeway.app.emju.mobile.model.ClientRequestInfo;
import com.safeway.app.emju.mylist.model.GroceryRewardsVO;
import com.safeway.app.emju.mylist.model.ShoppingVO;

@ImplementedBy(MyGroceryRewardsListServiceAPIImpl.class)
public interface MyGroceryRewardsListServiceAPI {

	GroceryRewardsVO getGroceryRewardsShoppingList(final ClientRequestInfo request, String details, String timestamp) throws MobileException;
}
