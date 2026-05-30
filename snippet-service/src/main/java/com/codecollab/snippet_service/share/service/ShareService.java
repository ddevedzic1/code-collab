package com.codecollab.snippet_service.share.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.service.BaseService;
import com.codecollab.snippet_service.share.dto.SharedSnippetResponseDto;
import com.codecollab.snippet_service.share.dto.ShareCreateDto;
import com.codecollab.snippet_service.share.dto.ShareResponseDto;
import com.codecollab.snippet_service.share.dto.ShareUpdateDto;
import com.codecollab.snippet_service.share.dto.ShareUserCreateDto;
import com.codecollab.snippet_service.share.dto.ShareUserResponseDto;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.model.SnippetShare;
import com.codecollab.snippet_service.share.model.SnippetShareUser;
import com.codecollab.snippet_service.share.repository.SnippetShareRepository;
import com.codecollab.snippet_service.share.repository.SnippetShareUserRepository;
import com.codecollab.snippet_service.snippet.dto.SnippetLanguageDto;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.snippet.repository.SnippetRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService extends BaseService {

	private final SnippetShareRepository snippetShareRepository;
	private final SnippetShareUserRepository snippetShareUserRepository;
	private final SnippetRepository snippetRepository;

	@Transactional
	public ShareResponseDto create(UUID snippetId, ShareCreateDto dto) {
		var snippet = getActiveSnippet(snippetId);

		if (snippetShareRepository.existsBySnippetId(snippetId)) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.share.already.exists"));
		}

		var share = new SnippetShare();
		share.setSnippet(snippet);
		share.setShareToken(UUID.randomUUID().toString());
		share.setShareType(dto.getShareType());
		share.setPermission(dto.getPermission());
		var saved = snippetShareRepository.save(share);

		log.info("Created share {} for snippet {}", saved.getId(), snippetId);
		return toShareResponseDto(saved);
	}

	@Transactional(readOnly = true)
	public ShareResponseDto getBySnippetId(UUID snippetId) {
		var share = getActiveShareBySnippetId(snippetId);
		return toShareResponseDto(share);
	}

	@Transactional(readOnly = true)
	public ShareResponseDto getById(UUID shareId) {
		var share = getActiveShare(shareId);
		return toShareResponseDto(share);
	}

	@Transactional
	public ShareResponseDto update(UUID shareId, ShareUpdateDto dto) {
		var share = getActiveShare(shareId);

		if (dto.getShareType() != null) {
			share.setShareType(dto.getShareType());
		}
		if (dto.getPermission() != null) {
			share.setPermission(dto.getPermission());
		}

		var saved = snippetShareRepository.save(share);
		log.info("Updated share {}", saved.getId());
		return toShareResponseDto(saved);
	}

	@Transactional
	public void softDelete(UUID shareId) {
		var share = getActiveShare(shareId);
		share.setEndDate(LocalDateTime.now());
		snippetShareRepository.save(share);
		log.info("Soft-deleted share {}", shareId);
	}

	@Transactional(readOnly = true)
	public List<ShareUserResponseDto> getUsers(UUID shareId) {
		getActiveShare(shareId);
		return snippetShareUserRepository.findBySnippetShareId(shareId).stream()
				.map(this::toUserResponseDto)
				.toList();
	}

	@Transactional
	public ShareUserResponseDto addUser(UUID shareId, ShareUserCreateDto dto) {
		var share = getActiveShare(shareId);

		if (snippetShareUserRepository.existsBySnippetShareIdAndUserId(share.getId(), dto.getUserId())) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.share.user.already.added"));
		}

		var entity = new SnippetShareUser();
		entity.setSnippetShare(share);
		entity.setUserId(dto.getUserId());
		entity.setPermission(dto.getPermission());
		var saved = snippetShareUserRepository.save(entity);

		log.info("Added user {} to share {}", dto.getUserId(), shareId);
		return modelMapper.map(saved, ShareUserResponseDto.class);
	}

	@Transactional
	public void removeUser(UUID shareId, UUID userId) {
		getActiveShare(shareId);
		var shareUser = snippetShareUserRepository.findBySnippetShareIdAndUserId(shareId, userId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.user.not.found")));
		shareUser.setEndDate(LocalDateTime.now());
		snippetShareUserRepository.save(shareUser);
		log.info("Removed user {} from share {}", userId, shareId);
	}

	@Transactional
	public void removeAllUsers(UUID shareId) {
		getActiveShare(shareId);
		var shareUsers = snippetShareUserRepository.findBySnippetShareId(shareId);
		var now = LocalDateTime.now();
		shareUsers.forEach(su -> su.setEndDate(now));
		snippetShareUserRepository.saveAll(shareUsers);
		log.info("Removed all {} users from share {}", shareUsers.size(), shareId);
	}

	@Transactional(readOnly = true)
	public SharedSnippetResponseDto getByToken(String token, UUID callerUserId) {
		var share = snippetShareRepository.findByShareToken(token)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.token.invalid")));

		Optional<SnippetShareUser> perUser = callerUserId == null
				? Optional.empty()
				: snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), callerUserId);

		if (share.getShareType() == ShareType.USER && perUser.isEmpty()) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.share.access.denied"));
		}

		var effectivePermission = perUser
				.map(SnippetShareUser::getPermission)
				.orElse(share.getPermission());

		var snippet = share.getSnippet();
		return toSharedSnippetResponseDto(snippet, effectivePermission);
	}

	private SnippetShare getActiveShare(UUID shareId) {
		return snippetShareRepository.findById(shareId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.not.found")));
	}

	private SnippetShare getActiveShareBySnippetId(UUID snippetId) {
		getActiveSnippet(snippetId);
		return snippetShareRepository.findBySnippetId(snippetId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.not.found")));
	}

	private Snippet getActiveSnippet(UUID snippetId) {
		return snippetRepository.findActiveById(snippetId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
	}

	private ShareResponseDto toShareResponseDto(SnippetShare share) {
		var dto = modelMapper.map(share, ShareResponseDto.class);
		dto.setSnippetId(share.getSnippet().getId());
		return dto;
	}

	private ShareUserResponseDto toUserResponseDto(SnippetShareUser shareUser) {
		return modelMapper.map(shareUser, ShareUserResponseDto.class);
	}

	private SharedSnippetResponseDto toSharedSnippetResponseDto(Snippet snippet, Permission permission) {
		var dto = new SharedSnippetResponseDto();
		dto.setSnippetId(snippet.getId());
		dto.setTitle(snippet.getTitle());
		dto.setContent(snippet.getContent());
		dto.setLanguage(modelMapper.map(snippet.getLanguage(), SnippetLanguageDto.class));
		dto.setPermission(permission);
		return dto;
	}
}
