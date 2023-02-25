package com.dellin.mondoc.model.pojo;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequestBuilder {
	
	@NotEmpty(message = "AppKey should not be empty") String appkey;
	@NotEmpty(message = "SessionID should not be empty") String sessionID;
	@NotEmpty(message = "Document UID (docuid) should not be empty") String docUid;
	@NotEmpty(message = "Document type (mode) should not be empty") String mode;
	
	public DocumentRequestBuilder setAppkey(String appkey) {
		this.appkey = appkey;
		return this;
	}
	
	public DocumentRequestBuilder setSessionID(String sessionID) {
		this.sessionID = sessionID;
		return this;
	}
	
	public DocumentRequestBuilder setDocUID(String docuid) {
		this.docUid = docuid;
		return this;
	}
	
	public DocumentRequestBuilder setMode(String mode) {
		this.mode = mode;
		return this;
	}
	
	public DocumentRequest build() {
		return new DocumentRequest(this);
	}
}
