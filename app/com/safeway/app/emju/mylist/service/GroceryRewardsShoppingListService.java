package com.safeway.app.emju.mylist.service;

import com.google.inject.ImplementedBy;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.mylist.model.GroceryRewardsVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

@ImplementedBy(GroceryRewardsShoppingListServiceImpl.class)
public interface GroceryRewardsShoppingListService {

	public GroceryRewardsVO getGroceryRewardsShoppingList(ShoppingListVO shoppingListVO) throws ApplicationException;
	
	

}
