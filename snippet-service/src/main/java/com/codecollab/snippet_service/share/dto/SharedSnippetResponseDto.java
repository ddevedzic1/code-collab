package com.codecollab.snippet_service.share.dto;

import java.util.UUID;

import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.snippet.dto.SnippetLanguageDto;

import lombok.Data;

@Data
public class SharedSnippetResponseDto {

	private UUID snippetId;
	private String title;
	private String content;
	private SnippetLanguageDto language;
	private Permission permission;
}
