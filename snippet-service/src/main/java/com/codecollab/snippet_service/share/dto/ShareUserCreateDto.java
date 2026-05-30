package com.codecollab.snippet_service.share.dto;

import java.util.UUID;

import com.codecollab.snippet_service.share.model.Permission;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareUserCreateDto {

	@NotNull(message = "{validation.share.user.userId.required}")
	private UUID userId;

	@NotNull(message = "{validation.share.user.permission.required}")
	private Permission permission;
}
