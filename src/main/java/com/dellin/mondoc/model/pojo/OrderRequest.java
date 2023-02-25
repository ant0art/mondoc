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
	
	public static OrderRequestBuilder builder() {
		return new OrderRequestBuilder();
	}
}
