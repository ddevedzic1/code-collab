package com.codecollab.snippet_service.language.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LanguageUpdateDto {

	@Size(max = 30, message = "{validation.language.code.size}")
	private String code;

	@Size(max = 300, message = "{validation.language.name.size}")
	private String name;

	@Size(max = 30, message = "{validation.language.version.size}")
	private String version;

	@Size(max = 300, message = "{validation.language.runtimeImage.size}")
	private String runtimeImage;

	private LocalDateTime startDate;

	private LocalDateTime endDate;
}
