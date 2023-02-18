package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface IInterfaceManualLoad {
	
	@Headers("Content-Type: application/json")
	@POST("/v3/auth/login.json")
	Call<AuthDellin> login(@Query("email") String email, @Body SessionDTO sessionDTO);
	
	//	@POST("v3/auth/logout.json")
	//	Call<AuthDellin> logout(@Body )
		
		/*@POST("/api/v2/registration")
		Call<UserSessionDTO> register(@Body RegisterRequest request);

		@POST("/api/auth/refresh")
		Call<UserSessionDTO> refresh(@Body UserLoginRequest request);

		@GET("/api/auth/refresh")
		Call<UserSessionDTO> reserveFN(@Body UserLoginRequest request);*/
	
}
