package com.codecollab.snippet_service.share.dto;

import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;

import lombok.Data;

@Data
public class ShareUpdateDto {

	private ShareType shareType;

	private Permission permission;
}
