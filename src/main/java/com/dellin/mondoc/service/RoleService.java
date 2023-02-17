package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.RoleDTO;

public interface RoleService {
	
	RoleDTO create(RoleDTO roleDTO);
	
	RoleDTO get(String roleName);
	
	void addRoleToUser(String email, String roleName);
}
