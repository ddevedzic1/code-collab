package com.codecollab.user_service.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequestDto {

	@NotBlank(message = "{validation.user.username.required}")
	@Size(min = 3, max = 300, message = "{validation.user.username.size}")
	private String username;

	@NotBlank(message = "{validation.user.email.required}")
	@Email(message = "{validation.user.email.invalid}")
	@Size(max = 300, message = "{validation.user.email.size}")
	private String email;

	@NotBlank(message = "{validation.user.password.required}")
	@Size(min = 8, max = 300, message = "{validation.user.password.size}")
	private String password;
}
