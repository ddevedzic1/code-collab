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
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.model.SnippetShare;
import com.codecollab.snippet_service.share.repository.SnippetShareRepository;
import com.codecollab.snippet_service.share.repository.SnippetShareUserRepository;
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
	private final SnippetShareRepository snippetShareRepository;
	private final SnippetShareUserRepository snippetShareUserRepository;

	@Transactional(readOnly = true)
	public PageResult<SnippetResponseDto> search(UUID callerUserId, String title, UUID languageId, Pageable pageable) {
		var titlePattern = (title == null || title.isBlank()) ? null : "%" + title.toLowerCase() + "%";
		var page = snippetRepository.search(callerUserId, titlePattern, languageId, pageable)
				.map(snippet -> modelMapper.map(snippet, SnippetResponseDto.class));
		return PageResult.from(page);
	}

	@Transactional(readOnly = true)
	public SnippetResponseDto getById(UUID id, UUID callerUserId) {
		var snippet = findOwnedSnippet(id, callerUserId);
		return modelMapper.map(snippet, SnippetResponseDto.class);
	}

	@Transactional
	public SnippetResponseDto create(SnippetCreateDto dto, UUID callerUserId) {
		var language = findActiveLanguage(dto.getLanguageId());

		var snippet = new Snippet();
		snippet.setUserId(callerUserId);
		snippet.setLanguage(language);
		snippet.setTitle(dto.getTitle());
		snippet.setContent(dto.getContent() != null ? dto.getContent() : "");

		var saved = snippetRepository.save(snippet);
		log.info("Created snippet {} for user {}", saved.getId(), saved.getUserId());
		return modelMapper.map(saved, SnippetResponseDto.class);
	}

	@Transactional
	public SnippetResponseDto update(UUID id, SnippetUpdateDto dto, UUID callerUserId) {
		var snippet = findEditableSnippet(id, callerUserId);

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
	public void softDelete(UUID id, UUID callerUserId) {
		var snippet = findOwnedSnippet(id, callerUserId);
		snippet.setEndDate(LocalDateTime.now());
		snippetRepository.save(snippet);
		log.info("Soft-deleted snippet {}", id);
	}

	private Snippet findOwnedSnippet(UUID id, UUID callerUserId) {
		var snippet = snippetRepository.findActiveById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
		if (!snippet.getUserId().equals(callerUserId)) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.snippet.forbidden"));
		}
		return snippet;
	}

	private Snippet findEditableSnippet(UUID id, UUID callerUserId) {
		var snippet = snippetRepository.findActiveById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
		if (snippet.getUserId().equals(callerUserId) || hasEditShare(id, callerUserId)) {
			return snippet;
		}
		throw new AppException(AppException.FORBIDDEN_ERROR,
				messages.get("error.snippet.forbidden"));
	}

	private boolean hasEditShare(UUID snippetId, UUID callerUserId) {
		if (callerUserId == null) {
			return false;
		}
		return snippetShareRepository.findBySnippetId(snippetId)
				.map(share -> grantsEdit(share, callerUserId))
				.orElse(false);
	}

	private boolean grantsEdit(SnippetShare share, UUID callerUserId) {
		if (share.getShareType() == ShareType.USER) {
			return snippetShareUserRepository
					.findBySnippetShareIdAndUserId(share.getId(), callerUserId)
					.map(shareUser -> shareUser.getPermission() == Permission.EDIT)
					.orElse(false);
		}
		return share.getPermission() == Permission.EDIT;
	}

	private Language findActiveLanguage(UUID languageId) {
		return languageRepository.findById(languageId)
				.orElseThrow(() -> new AppException(AppException.VALIDATION_ERROR,
						messages.get("error.snippet.language.invalid")));
	}
}
