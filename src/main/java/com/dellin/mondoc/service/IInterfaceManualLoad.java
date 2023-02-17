package com.dellin.mondoc.service;

import com.dellin.mondoc.model.pojo.AuthDellin;
import com.dellin.mondoc.model.pojo.UserLoginRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface IInterfaceManualLoad {
	
	//	@Headers("Content-Type: application/json")
	@POST("/v3/auth/login.json")
	Call<AuthDellin> login(@Body UserLoginRequest request);
		
		/*@POST("/api/v2/registration")
		Call<UserSessionDTO> register(@Body RegisterRequest request);

		@POST("/api/auth/refresh")
		Call<UserSessionDTO> refresh(@Body UserLoginRequest request);

		@GET("/api/auth/refresh")
		Call<UserSessionDTO> reserveFN(@Body UserLoginRequest request);*/
	
}
