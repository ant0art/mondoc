package com.dellin.mondoc.model.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DocumentRequest {
	
	@NotEmpty(message = "AppKey should not be empty") String appkey;
	@NotEmpty(message = "SessionID should not be empty") String sessionID;
	@NotEmpty(message = "Document UID (docuid) should not be empty") String docUid;
	@NotEmpty(message = "Document type (mode) should not be empty") String mode;
	
	public DocumentRequest(DocumentRequestBuilder documentRequestBuilder) {
		this.appkey = documentRequestBuilder.appkey;
		this.sessionID = documentRequestBuilder.sessionID;
		this.docUid = documentRequestBuilder.docUid;
		this.mode = documentRequestBuilder.mode;
	}
	
	public static DocumentRequestBuilder builder() {
		return new DocumentRequestBuilder();
	}
}
