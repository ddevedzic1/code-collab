package com.codecollab.user_service.user.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.user_service.controller.BaseController;
import com.codecollab.user_service.user.dto.UserLookupDto;
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

	@GetMapping("/by-username/{username}")
	public ResponseEntity<UserLookupDto> lookupByUsername(@PathVariable String username) {
		return ResponseEntity.ok(userService.lookupByUsername(username));
	}

	@GetMapping("/lookup")
	public ResponseEntity<List<UserLookupDto>> lookupByIds(@RequestParam List<UUID> ids) {
		return ResponseEntity.ok(userService.lookupByIds(ids));
	}

	@GetMapping("/{id}")
	public ResponseEntity<UserResponseDto> getById(@PathVariable UUID id,
			@RequestHeader("X-User-Id") UUID callerUserId) {
		return ResponseEntity.ok(userService.getById(id, callerUserId));
	}

	@PatchMapping("/{id}")
	public ResponseEntity<UserResponseDto> update(@PathVariable UUID id,
			@Valid @RequestBody UserUpdateDto dto,
			@RequestHeader("X-User-Id") UUID callerUserId) {
		return ResponseEntity.ok(userService.update(id, dto, callerUserId));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> softDelete(@PathVariable UUID id,
			@RequestHeader("X-User-Id") UUID callerUserId) {
		userService.softDelete(id, callerUserId);
		return ResponseEntity.noContent().build();
	}
}
