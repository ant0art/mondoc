package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.DocumentRequest;
import com.dellin.mondoc.model.pojo.DocumentResponse;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IInterfaceManualLoad {
	
	@POST("/v3/auth/login.json")
	Call<AuthDellin> login(@Body SessionDTO sessionDTO);
	
	@POST("/v3/auth/logout.json")
	Call<AuthDellin> logout(@Body SessionDTO sessionDTO);
	
	@POST("/v3/orders.json")
	Call<OrderResponse> update(@Body OrderRequest orderRequest);
	
	@POST("/v1/printable.json")
	Call<DocumentResponse> getPrintableDoc(@Body DocumentRequest documentRequest);
}
