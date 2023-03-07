package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.dto.CommentDTO;
import com.dellin.mondoc.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Comments", description = "The comment API. Contains operations to work "
		+ "with comments like add new one or add it to definite order")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class CommentController {
	
	private final CommentService commentService;
	
	@PutMapping("/add")
	@Operation(summary = "Add a new comment",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<CommentDTO> create(
			@RequestBody(required = false) CommentDTO commentDTO) {
		return commentService.create(commentDTO);
	}
	
	@PostMapping("/addToOrder")
	@Operation(summary = "Add a comment to an order",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<?> addToUser(@RequestBody CommentToOrderForm form) {
		
		commentService.addCommentToOrder(form.getDocId(), form.getId());
		return ResponseEntity.ok().build();
	}
	
	@PutMapping("/update")
	@Operation(summary = "Update comment",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<CommentDTO> update(@RequestBody CommentDTO commentDTO) {
		return commentService.update(commentDTO);
	}
}

@Data
class CommentToOrderForm {
	
	private String docId;
	private Long id;
}
