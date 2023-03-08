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
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service class to work with Comments
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
	
	/**
	 * Repository which contains comments
	 */
	private final CommentRepository commentRepository;
	
	/**
	 * User service class
	 */
	private final UserService userService;
	
	/**
	 * Order service class
	 */
	private final OrderService orderService;
	
	/**
	 * ObjectMapper for reading and writing JSON
	 */
	private final ObjectMapper mapper =
			JsonMapper.builder().addModule(new JavaTimeModule())
					  .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false).build();
	
	/**
	 * Method that creates a new {@link Comment} entity and write it to database
	 * <p>
	 * Returns a {@link ResponseEntity<CommentDTO>} object with http <b>200</b> status if
	 * well-created. The object passed to the method should contain the required
	 * <u>email</u>
	 * field. Creating data is possible only for authorized users, since any change is
	 * recorded in the history
	 *
	 * @param commentDTO the {@link CommentDTO} object to add
	 *
	 * @return the {@link ResponseEntity<CommentDTO>} object
	 */
	@Override
	public ResponseEntity<CommentDTO> create(CommentDTO commentDTO) {
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		
		if (commentDTO.getText() == null || commentDTO.getText().isEmpty()) {
			throw new CustomException("Comment text can`t be empty",
					HttpStatus.BAD_REQUEST);
		}
		
		//commentDTO --> comment
		Comment comment = mapper.convertValue(commentDTO, Comment.class);
		comment.setStatus(EntityStatus.CREATED);
		
		user.getComments().add(comment);
		comment.setUser(user);
		
		//comment --> commentDTO
		CommentDTO dto =
				mapper.convertValue(commentRepository.save(comment), CommentDTO.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	/**
	 * Method provides to add a comment to the order
	 * <p>
	 * Method parameters must belong to previously created objects. It is important to
	 * keep in mind that an order can have more than one comment, while one comment cannot
	 * apply to several orders.
	 *
	 * @param docId the value of {@link Order} required field
	 * @param id    the value of {@link Comment} required field
	 */
	@Override
	public void addCommentToOrder(String docId, Long id) {
		Comment comment = getComment(id);
		Order order = orderService.getOrder(docId);
		
		if (comment.getOrder() != null) {
			throw new CustomException(String.format(
					"Comment [ID: {%d}] is already added to order [ID: {%s}]", id,
					comment.getOrder().getDocId()), HttpStatus.BAD_REQUEST);
		}
		
		order.getComments().add(comment);
		order.setStatus(EntityStatus.UPDATED);
		order.setUpdatedAt(LocalDateTime.now());
		
		comment.setOrder(order);
		comment.setStatus(EntityStatus.UPDATED);
		comment.setUpdatedAt(LocalDateTime.now());
		
		commentRepository.save(comment);
		log.info("Comment [ID: {}] was added to order [ID: {}]", comment.getId(),
				order.getDocId());
	}
	
	/**
	 * Method that update current {@link Comment} entity found in database
	 * <p>
	 * Returns the ResponseEntity object with http <b>200</b> status if well-updated.
	 * Updating data is possible only for authorized users, since any change is recorded
	 * in the history
	 *
	 * @param commentDTO the {@link CommentDTO} object to update
	 *
	 * @return the {@link ResponseEntity<CommentDTO>} object
	 */
	@Override
	public ResponseEntity<CommentDTO> update(CommentDTO commentDTO) {
		
		Comment comment = getComment(commentDTO.getId());
		String text = commentDTO.getText();
		if (text == null || text.isEmpty()) {
			throw new CustomException("Comment text can`t be empty",
					HttpStatus.BAD_REQUEST);
		}
		comment.setText(text);
		
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userService.getUser(email);
		comment.setUser(user);
		comment.setStatus(EntityStatus.UPDATED);
		comment.setUpdatedAt(LocalDateTime.now());
		
		CommentDTO dto =
				mapper.convertValue(commentRepository.save(comment), CommentDTO.class);
		
		return ResponseEntity.status(HttpStatus.OK).body(dto);
	}
	
	/**
	 * Method that find a {@link Comment} in the database by id
	 * <p>
	 * Returns the Comment object if found or else the {@link CustomException} with http
	 * <b>404</b> status
	 *
	 * @param id the comment id
	 *
	 * @return the {@link Comment} object
	 */
	public Comment getComment(Long id) {
		return commentRepository.findById(id).orElseThrow(
				() -> new CustomException(String.format("Comment [ID: %d] not found", id),
						HttpStatus.NOT_FOUND));
	}
}
