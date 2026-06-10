package com.codecollab.snippet_service.share.dto;

import com.codecollab.snippet_service.share.model.Permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareUserCreateDto {

	@NotBlank(message = "{validation.share.user.username.required}")
	private String username;

	@NotNull(message = "{validation.share.user.permission.required}")
	private Permission permission;
}
