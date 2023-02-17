package com.dellin.mondoc.exceptions;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public class CustomException extends RuntimeException {
	
	private final String message;
	private final HttpStatus status;
}
