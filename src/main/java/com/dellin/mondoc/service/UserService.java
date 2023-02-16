package com.dellin.mondoc.service;

import com.dellin.mondoc.model.dto.UserDTO;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import org.springframework.data.domain.Sort;
import org.springframework.ui.ModelMap;

public interface UserService {

	UserDTO create(UserDTO userDTO);

	UserDTO get(String email);

	UserDTO update(String email, UserDTO userDTO);

	void delete(String email);

	User getUser(String email);

	ModelMap getUsers(Integer page, Integer perPage, String sort, Sort.Direction order);

	void updateStatus(User user, EntityStatus status);
}
