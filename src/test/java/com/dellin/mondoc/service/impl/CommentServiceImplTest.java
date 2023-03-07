package com.dellin.mondoc.service.impl;

import com.dellin.mondoc.exceptions.CustomException;
import com.dellin.mondoc.model.dto.CommentDTO;
import com.dellin.mondoc.model.entity.Comment;
import com.dellin.mondoc.model.entity.Order;
import com.dellin.mondoc.model.entity.User;
import com.dellin.mondoc.model.repository.CommentRepository;
import com.dellin.mondoc.service.OrderService;
import com.dellin.mondoc.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class CommentServiceImplTest {
	
	@InjectMocks
	private CommentServiceImpl commentService;
	@Mock
	private UserService userService;
	@Mock
	private OrderService orderService;
	@Mock
	private CommentRepository commentRepository;
	@Spy
	private ObjectMapper mapper;
	
	@Test
	public void create() {
		
		CommentDTO commentDTO = new CommentDTO();
		commentDTO.setText("some text");
		
		String name = "test@test.com";
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken(name, null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		User user = new User();
		user.setEmail(name);
		
		when(userService.getUser(anyString())).thenReturn(user);
		
		when(commentRepository.save(any(Comment.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		ResponseEntity<CommentDTO> resultDTO = commentService.create(commentDTO);
		
		assertEquals(commentDTO.getText(), resultDTO.getBody().getText());
	}
	
	@Test(expected = CustomException.class)
	public void create_emptyComment() {
		CommentDTO commentDTO = new CommentDTO();
		commentDTO.setText("");
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken("test@test.com", null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		User user = new User();
		when(userService.getUser(anyString())).thenReturn(user);
		commentService.create(commentDTO);
	}
	
	@Test
	public void addCommentToOrder() {
		
		Comment comment = new Comment();
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		
		Order order = new Order();
		order.setComments(new ArrayList<>());
		when(orderService.getOrder(anyString())).thenReturn(order);
		
		when(commentRepository.save(any(Comment.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		commentService.addCommentToOrder("0x1", 1L);
	}
	
	@Test(expected = CustomException.class)
	public void addCommentToOrder_commentExist() {
		
		Comment comment = new Comment();
		Order order = new Order();
		comment.setOrder(order);
		
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		when(orderService.getOrder(anyString())).thenReturn(order);
		
		lenient().when(commentRepository.save(any(Comment.class)))
				.thenAnswer(i -> i.getArguments()[0]);
		
		commentService.addCommentToOrder("0x1", 1L);
	}
	
	@Test
	public void update() {
		
		CommentDTO commentDTO = new CommentDTO();
		commentDTO.setText("some updated text");
		commentDTO.setId(1L);
		
		Comment comment = new Comment();
		comment.setText("some text");
		
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		
		comment.setText(commentDTO.getText());
		
		SecurityContext securityContext = mock(SecurityContext.class);
		Authentication a = new UsernamePasswordAuthenticationToken("test@test.com", null);
		
		when(securityContext.getAuthentication()).thenReturn(a);
		securityContext.setAuthentication(a);
		SecurityContextHolder.setContext(securityContext);
		
		User user = new User();
		when(userService.getUser(anyString())).thenReturn(user);
		
		when(commentRepository.save(any(Comment.class))).thenAnswer(
				i -> i.getArguments()[0]);
		
		ResponseEntity<CommentDTO> resultDTO = commentService.update(commentDTO);
		
		assertEquals(commentDTO.getText(), resultDTO.getBody().getText());
	}
	
	@Test(expected = CustomException.class)
	public void update_emptyCommentText() {
		
		CommentDTO commentDTO = new CommentDTO();
		commentDTO.setText("");
		commentDTO.setId(1L);
		
		Comment comment = new Comment();
		
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		
		commentService.update(commentDTO);
	}
	
	@Test(expected = CustomException.class)
	public void update_idNotFound() {
		
		commentService.update(new CommentDTO());
	}
	
	@Test
	public void getComment() {
		
		Comment comment = new Comment();
		comment.setText("some text");
		
		when(commentRepository.findById(anyLong())).thenReturn(Optional.of(comment));
		
		Comment result = commentService.getComment(1L);
		
		assertEquals(comment.getText(), result.getText());
	}
	
	@Test(expected = CustomException.class)
	public void getComment_idNotFound() {
		
		commentService.getComment(1L);
	}
}
