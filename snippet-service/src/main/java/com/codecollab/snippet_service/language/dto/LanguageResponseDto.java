package com.codecollab.snippet_service.language.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class LanguageResponseDto {

	private UUID id;
	private String code;
	private String name;
	private String version;
	private String runtimeImage;
	private LocalDateTime startDate;
	private LocalDateTime endDate;
}
