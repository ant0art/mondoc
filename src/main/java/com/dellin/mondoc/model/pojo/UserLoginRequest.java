package com.dellin.mondoc.model.pojo;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public final class UserLoginRequest {
	
	final String appkey = System.getenv("appkey");
	final String login = System.getenv("loginDl");
	final String password = System.getenv("passDl");
}
