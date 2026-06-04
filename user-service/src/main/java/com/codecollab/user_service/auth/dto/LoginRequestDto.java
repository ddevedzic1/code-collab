package com.codecollab.user_service.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {

	@NotBlank(message = "{validation.auth.username.required}")
	private String username;

	@NotBlank(message = "{validation.auth.password.required}")
	private String password;
}
