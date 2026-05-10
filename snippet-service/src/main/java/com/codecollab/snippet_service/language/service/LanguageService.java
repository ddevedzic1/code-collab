package com.codecollab.snippet_service.language.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.language.dto.LanguageCreateDto;
import com.codecollab.snippet_service.language.dto.LanguageResponseDto;
import com.codecollab.snippet_service.language.dto.LanguageUpdateDto;
import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.language.repository.LanguageRepository;
import com.codecollab.snippet_service.service.BaseService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LanguageService extends BaseService {

	private final LanguageRepository languageRepository;

	public List<LanguageResponseDto> getAll() {
		return languageRepository.findAll().stream()
				.map(language -> modelMapper.map(language, LanguageResponseDto.class))
				.toList();
	}

	public LanguageResponseDto getById(UUID id) {
		var language = findActiveById(id);
		return modelMapper.map(language, LanguageResponseDto.class);
	}

	public LanguageResponseDto create(LanguageCreateDto dto) {
		if (languageRepository.existsByCode(dto.getCode())) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.language.code.taken", dto.getCode()));
		}

		var startDate = dto.getStartDate() != null ? dto.getStartDate() : LocalDateTime.now();
		validateDateRange(startDate, dto.getEndDate());

		var language = modelMapper.map(dto, Language.class);
		language.setStartDate(startDate);

		var saved = languageRepository.save(language);
		log.info("Created language {} with code {}", saved.getId(), saved.getCode());
		return modelMapper.map(saved, LanguageResponseDto.class);
	}

	public LanguageResponseDto update(UUID id, LanguageUpdateDto dto) {
		var language = findActiveById(id);

		if (dto.getCode() != null && !dto.getCode().equals(language.getCode())) {
			if (languageRepository.existsByCode(dto.getCode())) {
				throw new AppException(AppException.VALIDATION_ERROR,
						messages.get("error.language.code.taken", dto.getCode()));
			}
			language.setCode(dto.getCode());
		}
		if (dto.getName() != null) {
			language.setName(dto.getName());
		}
		if (dto.getVersion() != null) {
			language.setVersion(dto.getVersion());
		}
		if (dto.getRuntimeImage() != null) {
			language.setRuntimeImage(dto.getRuntimeImage());
		}
		if (dto.getStartDate() != null) {
			language.setStartDate(dto.getStartDate());
		}
		if (dto.getEndDate() != null) {
			language.setEndDate(dto.getEndDate());
		}

		validateDateRange(language.getStartDate(), language.getEndDate());

		var saved = languageRepository.save(language);
		log.info("Updated language {}", saved.getId());
		return modelMapper.map(saved, LanguageResponseDto.class);
	}

	public void softDelete(UUID id) {
		var language = findActiveById(id);
		language.setEndDate(LocalDateTime.now());
		languageRepository.save(language);
		log.info("Soft-deleted language {}", id);
	}

	private Language findActiveById(UUID id) {
		return languageRepository.findById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.language.not.found")));
	}

	private void validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
		if (endDate != null && !endDate.isAfter(startDate)) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.language.endDate.beforeStart"));
		}
	}
}
