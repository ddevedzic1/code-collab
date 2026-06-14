package com.codecollab.snippet_service.snippet.dto;

import java.util.UUID;

import lombok.Data;

@Data
public class SnippetLanguageDto {

	private UUID id;
	private String code;
	private String name;
	private String version;
	private String runtimeImage;
}
