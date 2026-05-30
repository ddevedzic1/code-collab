package com.codecollab.snippet_service.share.dto;

import java.util.UUID;

import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;

import lombok.Data;

@Data
public class ShareResponseDto {

	private UUID id;
	private UUID snippetId;
	private String shareToken;
	private ShareType shareType;
	private Permission permission;
}
