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

/**
 * Interface of Retrofit methods that allows to send requests to and get responses from
 * API Dellin
 */
public interface IInterfaceManualLoad {
	
	/**
	 * An invocation of a Retrofit method that sends a request with {@link SessionDTO}
	 * body to a webserver and returns a response with {@link AuthDellin}
	 * <p>
	 * Method allows user to login in API Dellin
	 *
	 * @param sessionDTO the {@link SessionDTO} object for sending to API
	 *
	 * @return the {@link AuthDellin} response
	 */
	@POST("/v3/auth/login.json")
	Call<AuthDellin> login(@Body SessionDTO sessionDTO);
	
	/**
	 * An invocation of a Retrofit method that sends a request with {@link SessionDTO}
	 * body to a webserver and returns a response with {@link AuthDellin}
	 * <p>
	 * Method allows user to logout from API Dellin
	 *
	 * @param sessionDTO the {@link SessionDTO} object for sending to API
	 *
	 * @return the {@link AuthDellin} response
	 */
	@POST("/v3/auth/logout.json")
	Call<AuthDellin> logout(@Body SessionDTO sessionDTO);
	
	/**
	 * An invocation of a Retrofit method that sends a request with {@link OrderRequest}
	 * body to a webserver and returns a response with {@link OrderResponse}
	 * <p>
	 * Method allows user to request orders in API Dellin
	 *
	 * @param orderRequest the {@link OrderRequest} object for sending to API
	 *
	 * @return the {@link OrderResponse} response
	 */
	@POST("/v3/orders.json")
	Call<OrderResponse> update(@Body OrderRequest orderRequest);
	
	/**
	 * An invocation of a Retrofit method that sends a request with
	 * {@link DocumentRequest} body to a webserver and returns a response with
	 * {@link DocumentResponse}
	 * <p>
	 * Method allows user to request available printable documents in API Dellin
	 *
	 * @param documentRequest the {@link DocumentRequest} object for sending to API
	 *
	 * @return the {@link DocumentResponse} response
	 */
	@POST("/v1/printable.json")
	Call<DocumentResponse> getPrintableDoc(@Body DocumentRequest documentRequest);
}
