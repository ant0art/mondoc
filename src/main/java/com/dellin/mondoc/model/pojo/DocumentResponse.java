package com.dellin.mondoc.model.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.*;

@Getter
@Setter
public class DocumentResponse {
	
	Metadata metadata;
	Collection<Data> data;
	
	@lombok.Data
	public static class Metadata {
		
		Integer status;
		String generated_at;
	}
	
	@lombok.Data
	public static class Data {
		
		String uid;
		String base64;
		Collection<String> urls;
	}
}
