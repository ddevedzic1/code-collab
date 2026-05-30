package com.codecollab.snippet_service.share.dto;

import java.util.UUID;

import com.codecollab.snippet_service.share.model.Permission;

import lombok.Data;

@Data
public class ShareUserResponseDto {

	private UUID id;
	private UUID userId;
	private Permission permission;
}
