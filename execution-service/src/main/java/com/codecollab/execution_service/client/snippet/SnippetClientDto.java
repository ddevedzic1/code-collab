package com.codecollab.execution_service.client.snippet;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SnippetClientDto {

	private UUID id;
	private UUID userId;
	private LanguageClientDto language;
	private String content;
}
