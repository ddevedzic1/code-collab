package com.codecollab.snippet_service.language.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LanguageCreateDto {

	@NotBlank(message = "{validation.language.code.required}")
	@Size(max = 30, message = "{validation.language.code.size}")
	private String code;

	@NotBlank(message = "{validation.language.name.required}")
	@Size(max = 300, message = "{validation.language.name.size}")
	private String name;

	@NotBlank(message = "{validation.language.version.required}")
	@Size(max = 30, message = "{validation.language.version.size}")
	private String version;

	@NotBlank(message = "{validation.language.runtimeImage.required}")
	@Size(max = 300, message = "{validation.language.runtimeImage.size}")
	private String runtimeImage;

	private LocalDateTime startDate;

	private LocalDateTime endDate;
}
