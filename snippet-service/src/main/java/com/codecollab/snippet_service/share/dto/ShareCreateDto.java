package com.codecollab.snippet_service.share.dto;

import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShareCreateDto {

	@NotNull(message = "{validation.share.shareType.required}")
	private ShareType shareType;

	@NotNull(message = "{validation.share.permission.required}")
	private Permission permission;
}
