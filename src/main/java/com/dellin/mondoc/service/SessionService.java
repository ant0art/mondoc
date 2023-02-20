package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import java.io.*;

public interface SessionService {
	
	AuthDellin getLoginResponse(SessionDTO sessionDTO) throws IOException;
	
	AuthDellin getLogoutResponse() throws IOException;
	
	IInterfaceManualLoad getRemoteData();
	
}
