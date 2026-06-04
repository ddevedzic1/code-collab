package com.codecollab.user_service.auth.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SessionUserDto {

	private UUID id;
	private String username;
}
