package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.CommentDTO;
import com.dellin.mondoc.service.CommentService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
	
	private final CommentService commentService;
	
	@PutMapping("/add")
	public ResponseEntity<CommentDTO> create(
			@RequestBody(required = false) CommentDTO commentDTO) {
		return commentService.create(commentDTO);
	}
	
	@PostMapping("/addToOrder")
	public ResponseEntity<?> addToUser(@RequestBody CommentToOrderForm form) {
		
		commentService.addCommentToOrder(form.getDocId(), form.getId());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/update")
	public ResponseEntity<CommentDTO> update(@RequestBody CommentDTO commentDTO) {
		return ResponseEntity.ok(commentService.update(commentDTO));
	}
}

@Data
class CommentToOrderForm {
	
	private String docId;
	private Long id;
}
