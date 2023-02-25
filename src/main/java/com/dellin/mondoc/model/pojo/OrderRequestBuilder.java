package com.dellin.mondoc.model.pojo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.*;
import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
public class OrderRequestBuilder {
	
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
