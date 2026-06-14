package com.codecollab.snippet_service.share.dto;

import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.validation.PermissionRequiredForPublicLink;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@PermissionRequiredForPublicLink
public class ShareCreateDto {

	@NotNull(message = "{validation.share.shareType.required}")
	private ShareType shareType;

	private Permission permission;
}
