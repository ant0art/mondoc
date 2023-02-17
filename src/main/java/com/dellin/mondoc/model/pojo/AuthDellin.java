package com.dellin.mondoc.model.pojo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthDellin {
	
	public Metadata metadata;
	public Data data;
	
	public class Metadata {
		
		public Integer status;
		public String generated_at;
	}
	
	public class Data {
		
		public String sessionID;
	}
}


