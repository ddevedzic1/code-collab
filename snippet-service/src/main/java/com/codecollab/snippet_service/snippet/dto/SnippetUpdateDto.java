package com.codecollab.snippet_service.snippet.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SnippetUpdateDto {

	private UUID languageId;

	@Size(max = 300, message = "{validation.snippet.title.size}")
	private String title;

	private String content;
}
