package com.dellin.mondoc.controllers;

import com.dellin.mondoc.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "The document API. Contains operations to "
		+ "manually start and stop updating data of the available documents")
@SecurityScheme(type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT",
				name = "Authorization")
public class DocumentController {
	
	private final DocumentService documentService;
	
	@PostMapping(value = "/update")
	@Operation(summary = "Update documents",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<?> update(@RequestParam(required = false) String uid,
			@RequestParam(required = false) String type) throws IOException {
		
		documentService.update();
		return ResponseEntity.ok().build();
	}
	
	@GetMapping("/stopUpdate")
	@Operation(summary = "Interrupt process of updating  documents",
			   security = @SecurityRequirement(name = "Authorization"))
	public ResponseEntity<?> stopUpdate() {
		documentService.stopUpdate();
		return ResponseEntity.ok().build();
	}
}
