package com.codecollab.user_service.user.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserResponseDto {

	private UUID id;
	private String username;
	private String email;
}
