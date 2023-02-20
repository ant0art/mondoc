package com.dellin.mondoc.model.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDellin {
	
	private Metadata metadata;
	private Data data;
	
	@Getter
	@Setter
	public class Metadata {
		
		private Integer status;
		private String generated_at;
	}
	
	@Getter
	@Setter
	public class Data {
		
		private String state;
		private String sessionID;
	}
}


