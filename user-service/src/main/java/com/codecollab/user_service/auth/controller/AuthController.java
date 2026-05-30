package com.codecollab.user_service.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.user_service.auth.dto.RegisterRequestDto;
import com.codecollab.user_service.auth.service.AuthService;
import com.codecollab.user_service.controller.BaseController;
import com.codecollab.user_service.user.dto.UserResponseDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

	private final AuthService authService;

	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
		var response = authService.register(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
}
