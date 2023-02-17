package com.dellin.mondoc.service;

import com.dellin.mondoc.model.pojo.AuthDellin;
import java.io.*;

public interface AuthDellinService {
	
	IInterfaceManualLoad getRemoteData();
	
	AuthDellin getLoginResponse() throws IOException;
}
