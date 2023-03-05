package com.dellin.mondoc.service;

import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderRequestBuilder;
import com.dellin.mondoc.model.pojo.OrderResponse;
import java.io.*;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.ModelMap;

import java.util.*;

public interface OrderService {
	
	@Transactional
	void update(OrderRequest orderRequest) throws IOException;
	
	void extracted(Thread thread, OrderRequest orderRequest, User user, int currentPage,
			int totalPages, OrderRequestBuilder requestBuilder, Date programStart);
	
	void createAndUpdateOrders(Collection<OrderResponse.Order> orders);
	
	//IInterfaceManualLoad getRemoteData();
	
	Order getOrder(String docId);
	
	ModelMap getOrders(Integer page, Integer perPage, String sort, Sort.Direction order);
	
	void stopUpdate();
}
