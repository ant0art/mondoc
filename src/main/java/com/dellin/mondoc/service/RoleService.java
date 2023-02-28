package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.RoleDTO;
import org.springframework.transaction.annotation.Transactional;

public interface RoleService {
	
	RoleDTO create(RoleDTO roleDTO);
	
	RoleDTO get(String roleName);
	
	@Transactional
	void addRoleToUser(String email, String roleName);
}
