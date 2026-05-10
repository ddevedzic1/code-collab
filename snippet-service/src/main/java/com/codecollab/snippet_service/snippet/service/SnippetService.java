package com.codecollab.snippet_service.snippet.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.language.repository.LanguageRepository;
import com.codecollab.snippet_service.service.BaseService;
import com.codecollab.snippet_service.snippet.dto.SnippetCreateDto;
import com.codecollab.snippet_service.snippet.dto.SnippetResponseDto;
import com.codecollab.snippet_service.snippet.dto.SnippetUpdateDto;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.snippet.repository.SnippetRepository;
import com.codecollab.snippet_service.util.PageResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnippetService extends BaseService {

	private final SnippetRepository snippetRepository;
	private final LanguageRepository languageRepository;

	@Transactional(readOnly = true)
	public PageResult<SnippetResponseDto> search(UUID userId, String title, UUID languageId, Pageable pageable) {
		var titlePattern = (title == null || title.isBlank()) ? null : "%" + title.toLowerCase() + "%";
		var page = snippetRepository.search(userId, titlePattern, languageId, pageable)
				.map(snippet -> modelMapper.map(snippet, SnippetResponseDto.class));
		return PageResult.from(page);
	}

	@Transactional(readOnly = true)
	public SnippetResponseDto getById(UUID id) {
		var snippet = snippetRepository.findWithLanguageById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
		return modelMapper.map(snippet, SnippetResponseDto.class);
	}

	@Transactional
	public SnippetResponseDto create(SnippetCreateDto dto) {
		if (dto.getUserId() == null) {
			throw new AppException(AppException.INTERNAL_ERROR,
					"Snippet create reached service without userId; expected to be supplied by client or gateway");
		}

		var language = findActiveLanguage(dto.getLanguageId());

		var snippet = new Snippet();
		snippet.setUserId(dto.getUserId());
		snippet.setLanguage(language);
		snippet.setTitle(dto.getTitle());
		snippet.setContent(dto.getContent() != null ? dto.getContent() : "");

		var saved = snippetRepository.save(snippet);
		log.info("Created snippet {} for user {}", saved.getId(), saved.getUserId());
		return modelMapper.map(saved, SnippetResponseDto.class);
	}

	@Transactional
	public SnippetResponseDto update(UUID id, SnippetUpdateDto dto) {
		var snippet = findActiveById(id);

		if (dto.getLanguageId() != null && !dto.getLanguageId().equals(snippet.getLanguage().getId())) {
			snippet.setLanguage(findActiveLanguage(dto.getLanguageId()));
		}
		if (dto.getTitle() != null) {
			snippet.setTitle(dto.getTitle());
		}
		if (dto.getContent() != null) {
			snippet.setContent(dto.getContent());
		}

		var saved = snippetRepository.save(snippet);
		log.info("Updated snippet {}", saved.getId());
		return modelMapper.map(saved, SnippetResponseDto.class);
	}

	@Transactional
	public void softDelete(UUID id) {
		var snippet = findActiveById(id);
		snippet.setEndDate(LocalDateTime.now());
		snippetRepository.save(snippet);
		log.info("Soft-deleted snippet {}", id);
	}

	private Snippet findActiveById(UUID id) {
		return snippetRepository.findWithLanguageById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
	}

	private Language findActiveLanguage(UUID languageId) {
		return languageRepository.findById(languageId)
				.orElseThrow(() -> new AppException(AppException.VALIDATION_ERROR,
						messages.get("error.snippet.language.invalid")));
	}
}
