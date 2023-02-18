package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.SessionDTO;
import com.dellin.mondoc.model.pojo.AuthDellin;
import java.io.*;

public interface SessionService {
	
	AuthDellin getLoginResponse(String email, SessionDTO sessionDTO) throws IOException;
	
	IInterfaceManualLoad getRemoteData();
	
}
