package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface IInterfaceManualLoad {
	
	@Headers("Content-Type: application/json")
	@POST("/v3/auth/login.json")
	Call<AuthDellin> login(@Body SessionDTO sessionDTO);
	
	@Headers("Content-Type: application/json")
	@POST("/v3/auth/logout.json")
	Call<AuthDellin> logout(@Body SessionDTO sessionDTO);
	
	@Headers("Content-Type: application/json")
	@POST("/v3/orders.json")
	Call<OrderResponse> getOrders(@Body OrderRequest orderRequest);
	
		/*@POST("/api/v2/registration")
		Call<UserSessionDTO> register(@Body RegisterRequest request);

		@POST("/api/auth/refresh")
		Call<UserSessionDTO> refresh(@Body UserLoginRequest request);

		@GET("/api/auth/refresh")
		Call<UserSessionDTO> reserveFN(@Body UserLoginRequest request);*/
}
