package com.dellin.mondoc.controllers;

import com.dellin.mondoc.model.pojo.OrderRequest;
import com.dellin.mondoc.model.pojo.OrderResponse;
import com.dellin.mondoc.service.OrderService;
import java.io.*;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OderController {
	
	private final OrderService orderService;
	
	@PostMapping(value = "/get", consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<OrderResponse> get(
			@RequestBody OrderRequest orderRequest) throws IOException {
		
		URI uri = URI.create(
				ServletUriComponentsBuilder.fromCurrentContextPath().toUriString());
		
		OrderResponse orderResponse = orderService.getOrderResponse(orderRequest);
		
		return ResponseEntity.created(uri).body(orderResponse);
	}
}
