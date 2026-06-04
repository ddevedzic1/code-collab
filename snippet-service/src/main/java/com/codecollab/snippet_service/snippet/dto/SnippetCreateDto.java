package com.codecollab.snippet_service.snippet.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SnippetCreateDto {

	@NotNull(message = "{validation.snippet.languageId.required}")
	private UUID languageId;

	@NotBlank(message = "{validation.snippet.title.required}")
	@Size(max = 300, message = "{validation.snippet.title.size}")
	private String title;

	private String content;
}
