package com.safeway.app.emju.mylist.service;

import java.util.Map;

import com.safeway.app.emju.exception.ApplicationException;
import com.safeway.app.emju.mylist.entity.ShoppingListItem;
import com.safeway.app.emju.mylist.model.ShoppingListVO;

import play.libs.F.Promise;
import scala.concurrent.ExecutionContext;

public interface ItemDetailAsyncRetriever<T> {
	
	public Promise<Map<String, T>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO) throws ApplicationException;
	
	public Promise<Map<String, T>> getAsyncDetails(String itemType, Map<String, ShoppingListItem> itemMap,
			ShoppingListVO shoppingListVO, ExecutionContext threadCtx) throws ApplicationException;
	
	public Map<Long, T>  getDetailsPromiseResult(Promise<Map<Long, T>> promiseItemDetail) 
    		throws ApplicationException;

}
