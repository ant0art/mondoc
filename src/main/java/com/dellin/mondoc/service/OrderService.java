package com.dellin.mondoc.service;

import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderResponse;
import java.io.*;

public interface OrderService {
	
	OrderResponse getOrderResponse(OrderRequest orderRequest) throws IOException;
	
	IInterfaceManualLoad getRemoteData();
}
