package com.codecollab.snippet_service.language.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.language.dto.LanguageCreateDto;
import com.codecollab.snippet_service.language.dto.LanguageUpdateDto;
import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.language.repository.LanguageRepository;
import com.codecollab.snippet_service.util.Messages;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

	@Mock
	private LanguageRepository languageRepository;

	@Mock
	private Messages messages;

	@InjectMocks
	private LanguageService languageService;

	private final UUID languageId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(languageService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(languageService, "messages", messages);
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private Language language() {
		var language = new Language();
		language.setId(languageId);
		language.setCode("python");
		language.setName("Python");
		language.setVersion("3.11");
		language.setRuntimeImage("python:3.11-slim");
		language.setStartDate(LocalDateTime.now());
		return language;
	}

	private LanguageCreateDto createDto() {
		var dto = new LanguageCreateDto();
		dto.setCode("rust");
		dto.setName("Rust");
		dto.setVersion("1.80");
		dto.setRuntimeImage("rust:1.80-slim");
		return dto;
	}

	@Test
	void getById_throwsNotFound_whenMissing() {
		when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> languageService.getById(languageId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void create_persists_whenCodeIsFree() {
		var dto = createDto();
		when(languageRepository.existsByCode("rust")).thenReturn(false);
		when(languageRepository.save(any(Language.class))).thenAnswer(call -> call.getArgument(0));

		var result = languageService.create(dto);

		assertThat(result.getCode()).isEqualTo("rust");
		assertThat(result.getRuntimeImage()).isEqualTo("rust:1.80-slim");
	}

	@Test
	void create_throwsValidation_whenCodeTaken() {
		var dto = createDto();
		when(languageRepository.existsByCode("rust")).thenReturn(true);

		assertThatThrownBy(() -> languageService.create(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
		verify(languageRepository, never()).save(any());
	}

	@Test
	void create_throwsValidation_whenEndDateBeforeStartDate() {
		var dto = createDto();
		var start = LocalDateTime.now();
		dto.setStartDate(start);
		dto.setEndDate(start.minusDays(1));
		when(languageRepository.existsByCode("rust")).thenReturn(false);

		assertThatThrownBy(() -> languageService.create(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void update_throwsValidation_whenNewCodeTaken() {
		var dto = new LanguageUpdateDto();
		dto.setCode("js");
		when(languageRepository.findById(languageId)).thenReturn(Optional.of(language()));
		when(languageRepository.existsByCode("js")).thenReturn(true);

		assertThatThrownBy(() -> languageService.update(languageId, dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void update_keepsCode_whenUnchanged() {
		var dto = new LanguageUpdateDto();
		dto.setCode("python");
		dto.setName("Python 3");
		when(languageRepository.findById(languageId)).thenReturn(Optional.of(language()));
		when(languageRepository.save(any(Language.class))).thenAnswer(call -> call.getArgument(0));

		var result = languageService.update(languageId, dto);

		assertThat(result.getName()).isEqualTo("Python 3");
		verify(languageRepository, never()).existsByCode(any());
	}

	@Test
	void softDelete_setsEndDate() {
		var language = language();
		when(languageRepository.findById(languageId)).thenReturn(Optional.of(language));
		when(languageRepository.save(any(Language.class))).thenAnswer(call -> call.getArgument(0));

		languageService.softDelete(languageId);

		assertThat(language.getEndDate()).isNotNull();
	}
}
