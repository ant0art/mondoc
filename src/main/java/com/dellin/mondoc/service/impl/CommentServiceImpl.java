package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.CommentDTO;
import com.dellin.mondoc.model.entity.Comment;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.enums.EntityStatus;
import com.dellin.mondoc.model.repository.CommentRepository;
import com.dellin.mondoc.service.CommentService;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
	
	private final CommentRepository commentRepository;
	
	private final UserService userService;
	
	private final OrderService orderService;
	
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule()).build();
	
	@Override
	public ResponseEntity<CommentDTO> create(CommentDTO commentDTO) {
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		
		User user = userService.getUser(email);
		
		Comment comment = new Comment();
		
		comment.setStatus(EntityStatus.CREATED);
		
		Collection<User> users = comment.getUsers();
		users.add(user);
		comment.setUsers(users);
		
		comment.setText(commentDTO.getText());
		
		CommentDTO dto =
				mapper.convertValue(commentRepository.save(comment), CommentDTO.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	@Override
	public void addCommentToOrder(String docId, Long id) {
		Comment comment = getComment(id);
		Order order = orderService.getOrder(docId);
		
		order.getComments().add(comment);
		order.setStatus(EntityStatus.UPDATED);
		order.setUpdatedAt(LocalDateTime.now());
		
		comment.setOrder(order);
		comment.setStatus(EntityStatus.UPDATED);
		comment.setUpdatedAt(LocalDateTime.now());
		
		commentRepository.save(comment);
	}
	
	@Override
	public CommentDTO update(CommentDTO commentDTO) {
		
		Comment comment = getComment(commentDTO.getId());
		String text = commentDTO.getText();
		if (text == null || text.isEmpty()) {
			throw new CustomException("Comment can`t be empty", HttpStatus.BAD_REQUEST);
		}
		comment.setText(text);
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		comment.getUsers().add(user);
		comment.setStatus(EntityStatus.UPDATED);
		comment.setUpdatedAt(LocalDateTime.now());
		
		return mapper.convertValue(commentRepository.save(comment), CommentDTO.class);
	}
	
	public Comment getComment(Long id) {
		return commentRepository.findById(id).orElseThrow(() -> new CustomException(
				String.format("Comment with ID: %d not found", id),
				HttpStatus.NOT_FOUND));
	}
}