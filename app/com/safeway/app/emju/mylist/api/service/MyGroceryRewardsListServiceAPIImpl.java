package com.safeway.app.emju.mylist.api.service;

import com.google.inject.Inject;
import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.logging.Logger;
import com.safeway.app.emju.logging.LoggerFactory;
import com.safeway.app.emju.mobile.exception.MobileException;
import com.safeway.app.emju.mobile.model.ClientRequestInfo;
import com.safeway.app.emju.mylist.api.util.TransformUtil;
import com.safeway.app.emju.mylist.model.GroceryRewardsVO;
import com.safeway.app.emju.mylist.model.ShoppingListVO;
import com.safeway.app.emju.mylist.service.GroceryRewardsShoppingListService;

public class MyGroceryRewardsListServiceAPIImpl implements MyGroceryRewardsListServiceAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(MyGroceryRewardsListServiceAPIImpl.class);
	private GroceryRewardsShoppingListService grShoppingListService;
	
	@Inject
	public MyGroceryRewardsListServiceAPIImpl(GroceryRewardsShoppingListService grShoppingListService) {
		this.grShoppingListService = grShoppingListService;
	}

	@Override
	public GroceryRewardsVO getGroceryRewardsShoppingList(ClientRequestInfo request, String details, String timestamp)
			throws MobileException {
		
		LOGGER.info("Inside getGroceryRewardsShoppingList");
		GroceryRewardsVO groceryRewardsVO;
		
		try {
			ShoppingListVO shoppingListVo = TransformUtil.getShoppingListVO(request);
			
			shoppingListVo.getHeaderVO().setDetails(details);
	        shoppingListVo.getHeaderVO().setTimestamp(timestamp);
	        shoppingListVo.getHeaderVO().setTimeZone(request.getTimeZone());
	        groceryRewardsVO = grShoppingListService.getGroceryRewardsShoppingList(shoppingListVo);
            
        } catch (ApplicationException e) {
        	LOGGER.error("Error when invoking shoppingListService.getShoppingList", e);
			throw new MobileException(e);
		}
        
		return groceryRewardsVO;
	}

}
