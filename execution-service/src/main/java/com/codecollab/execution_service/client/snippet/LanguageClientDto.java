package com.codecollab.execution_service.client.snippet;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LanguageClientDto {

	private UUID id;
	private String runtimeImage;
}
