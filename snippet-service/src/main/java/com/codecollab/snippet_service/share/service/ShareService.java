package com.codecollab.snippet_service.share.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.snippet_service.client.user.UserClient;
import com.codecollab.snippet_service.client.user.UserLookupClientDto;
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

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareService extends BaseService {

	private final SnippetShareRepository snippetShareRepository;
	private final SnippetShareUserRepository snippetShareUserRepository;
	private final SnippetRepository snippetRepository;
	private final UserClient userClient;

	@Transactional
	public ShareResponseDto create(UUID snippetId, ShareCreateDto dto, UUID callerUserId) {
		var snippet = getOwnedSnippet(snippetId, callerUserId);

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
	public ShareResponseDto getBySnippetId(UUID snippetId, UUID callerUserId) {
		getOwnedSnippet(snippetId, callerUserId);
		var share = snippetShareRepository.findBySnippetId(snippetId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.not.found")));
		return toShareResponseDto(share);
	}

	@Transactional(readOnly = true)
	public ShareResponseDto getById(UUID shareId, UUID callerUserId) {
		var share = getOwnedShare(shareId, callerUserId);
		return toShareResponseDto(share);
	}

	@Transactional
	public ShareResponseDto update(UUID shareId, ShareUpdateDto dto, UUID callerUserId) {
		var share = getOwnedShare(shareId, callerUserId);

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
	public void softDelete(UUID shareId, UUID callerUserId) {
		var share = getOwnedShare(shareId, callerUserId);
		share.setEndDate(LocalDateTime.now());
		snippetShareRepository.save(share);
		log.info("Soft-deleted share {}", shareId);
	}

	@Transactional(readOnly = true)
	public List<ShareUserResponseDto> getUsers(UUID shareId, UUID callerUserId) {
		getOwnedShare(shareId, callerUserId);
		var responses = snippetShareUserRepository.findBySnippetShareId(shareId).stream()
				.map(this::toUserResponseDto)
				.toList();

		var usernamesById = resolveUsernames(responses.stream()
				.map(ShareUserResponseDto::getUserId)
				.toList());
		responses.forEach(response -> response.setUsername(usernamesById.get(response.getUserId())));

		return responses;
	}

	private Map<UUID, String> resolveUsernames(List<UUID> userIds) {
		if (userIds.isEmpty()) {
			return Map.of();
		}
		try {
			return userClient.lookupByIds(userIds).stream()
					.collect(Collectors.toMap(UserLookupClientDto::getId, UserLookupClientDto::getUsername));
		} catch (Exception ex) {
			log.warn("Failed to resolve usernames for share users: {}", ex.getMessage());
			return Map.of();
		}
	}

	@Transactional
	public ShareUserResponseDto addUser(UUID shareId, ShareUserCreateDto dto, UUID callerUserId) {
		var share = getOwnedShare(shareId, callerUserId);

		var targetUser = resolveUser(dto.getUsername());

		if (targetUser.getId().equals(callerUserId)) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.share.user.self"));
		}
		if (snippetShareUserRepository.existsBySnippetShareIdAndUserId(share.getId(), targetUser.getId())) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.share.user.already.added"));
		}

		var entity = new SnippetShareUser();
		entity.setSnippetShare(share);
		entity.setUserId(targetUser.getId());
		entity.setPermission(dto.getPermission());
		var saved = snippetShareUserRepository.save(entity);

		log.info("Added user {} ({}) to share {}", targetUser.getUsername(), targetUser.getId(), shareId);
		var response = modelMapper.map(saved, ShareUserResponseDto.class);
		response.setUsername(targetUser.getUsername());
		return response;
	}

	@Transactional
	public void removeUser(UUID shareId, String username, UUID callerUserId) {
		getOwnedShare(shareId, callerUserId);
		var targetUser = resolveUser(username);
		var shareUser = snippetShareUserRepository.findBySnippetShareIdAndUserId(shareId, targetUser.getId())
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.user.not.found")));
		shareUser.setEndDate(LocalDateTime.now());
		snippetShareUserRepository.save(shareUser);
		log.info("Removed user {} ({}) from share {}", username, targetUser.getId(), shareId);
	}

	@Transactional
	public void removeAllUsers(UUID shareId, UUID callerUserId) {
		getOwnedShare(shareId, callerUserId);
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

		var snippet = share.getSnippet();

		if (callerUserId != null && snippet.getUserId().equals(callerUserId)) {
			return toSharedSnippetResponseDto(snippet, Permission.EDIT);
		}

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

		return toSharedSnippetResponseDto(snippet, effectivePermission);
	}

	private UserLookupClientDto resolveUser(String username) {
		try {
			return userClient.lookupByUsername(username);
		} catch (Exception ex) {
			var feign = unwrapFeign(ex);
			if (feign instanceof FeignException.NotFound) {
				throw new AppException(AppException.VALIDATION_ERROR,
						messages.get("error.share.user.username.not.found", username));
			}
			log.warn("User lookup failed for username {}: {}", username, ex.getMessage());
			throw new AppException(AppException.SERVICE_UNAVAILABLE_ERROR,
					messages.get("error.system.unavailable"));
		}
	}

	private static FeignException unwrapFeign(Throwable t) {
		if (t instanceof FeignException fe) {
			return fe;
		}
		if (t.getCause() instanceof FeignException fe) {
			return fe;
		}
		return null;
	}

	private SnippetShare getOwnedShare(UUID shareId, UUID callerUserId) {
		var share = snippetShareRepository.findById(shareId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.share.not.found")));
		if (!share.getSnippet().getUserId().equals(callerUserId)) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.share.forbidden"));
		}
		return share;
	}

	private Snippet getOwnedSnippet(UUID snippetId, UUID callerUserId) {
		var snippet = snippetRepository.findActiveById(snippetId)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.snippet.not.found")));
		if (!snippet.getUserId().equals(callerUserId)) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.share.forbidden"));
		}
		return snippet;
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
