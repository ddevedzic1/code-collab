package com.codecollab.user_service.user.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.user_service.controller.BaseController;
import com.codecollab.user_service.user.dto.UserResponseDto;
import com.codecollab.user_service.user.dto.UserUpdateDto;
import com.codecollab.user_service.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController extends BaseController {

	private final UserService userService;

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id) {
		return ResponseEntity.ok(userService.getById(id));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UserResponseDto> update(@PathVariable UUID id, @Valid @RequestBody UserUpdateDto dto) {
		return ResponseEntity.ok(userService.update(id, dto));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> softDelete(@PathVariable UUID id) {
		userService.softDelete(id);
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}
}
