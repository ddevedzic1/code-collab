package com.codecollab.user_service.user.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class UserLookupDto {

	private UUID id;
	private String username;
}
