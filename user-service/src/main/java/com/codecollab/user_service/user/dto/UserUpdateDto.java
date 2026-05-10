package com.codecollab.user_service.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {

	@Size(min = 3, max = 300, message = "{validation.user.username.size}")
	private String username;

	@Size(min = 8, max = 300, message = "{validation.user.password.size}")
	private String password;
}
