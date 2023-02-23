package com.dellin.mondoc.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.*;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest {
	
	@NotEmpty(message = "AppKey should not be empty") String appKey;
	@NotEmpty(message = "SessionID should not be empty") String sessionID;
	Collection<String> docIds;
	//	String orderNumber;
	//	String orderDate;
	//	String barcode;
	//	Collection<String> cargoPlaces;
	//	String shipmentLabelCargoPlace;
	String dateStart;
	String dateEnd;
	//	String states;
	Integer page;
	//	String lastUpdate;
	//	String orderBy;
	//	Boolean orderDatesAdditional;
	
	public OrderRequest(OrderRequestBuilder orderRequestBuilder) {
		this.appKey = orderRequestBuilder.appKey;
		this.sessionID = orderRequestBuilder.sessionID;
		this.docIds = orderRequestBuilder.docIds;
		this.dateStart = orderRequestBuilder.dateStart;
		this.dateEnd = orderRequestBuilder.dateEnd;
		this.page = orderRequestBuilder.page;
	}
	
	public static OrderRequest.OrderRequestBuilder builder() {
		return new OrderRequestBuilder();
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	public static class OrderRequestBuilder {
		
		@NotEmpty(message = "AppKey should not be empty") String appKey;
		@NotEmpty(message = "SessionID should not be empty") String sessionID;
		Collection<String> docIds;
		String dateStart;
		String dateEnd;
		Integer page;
		
		public OrderRequestBuilder setAppKey(String appKey) {
			this.appKey = appKey;
			return this;
		}
		
		public OrderRequestBuilder setSessionID(String sessionID) {
			this.sessionID = sessionID;
			return this;
		}
		
		public OrderRequestBuilder setDocIds(Collection<String> docIds) {
			this.docIds = docIds;
			return this;
		}
		
		public OrderRequestBuilder setDateStart(String dateStart) {
			this.dateStart = dateStart;
			return this;
		}
		
		public OrderRequestBuilder setDateEnd(String dateEnd) {
			this.dateEnd = dateEnd;
			return this;
		}
		
		public OrderRequestBuilder setPage(Integer page) {
			this.page = page;
			return this;
		}
		
		public OrderRequest build() {
			return new OrderRequest(this);
		}
	}
}
