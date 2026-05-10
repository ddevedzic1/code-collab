package com.codecollab.snippet_service.snippet.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SnippetResponseDto {

	private UUID id;
	private UUID userId;
	private SnippetLanguageDto language;
	private String title;
	private String content;
}
