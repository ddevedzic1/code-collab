package com.codecollab.user_service.user.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.service.BaseService;
import com.codecollab.user_service.user.dto.UserLookupDto;
import com.codecollab.user_service.user.dto.UserResponseDto;
import com.codecollab.user_service.user.dto.UserUpdateDto;
import com.codecollab.user_service.user.model.User;
import com.codecollab.user_service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService extends BaseService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserResponseDto getById(UUID id, UUID callerUserId) {
		var user = findOwnedUser(id, callerUserId);
		return modelMapper.map(user, UserResponseDto.class);
	}

	public UserLookupDto lookupByUsername(String username) {
		var user = userRepository.findByUsername(username)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.user.username.not.found", username)));
		return modelMapper.map(user, UserLookupDto.class);
	}

	public List<UserLookupDto> lookupByIds(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}
		return userRepository.findAllById(ids).stream()
				.map(user -> modelMapper.map(user, UserLookupDto.class))
				.toList();
	}

	public UserResponseDto update(UUID id, UserUpdateDto dto, UUID callerUserId) {
		var user = findOwnedUser(id, callerUserId);

		if (dto.getUsername() != null && !dto.getUsername().equals(user.getUsername())) {
			if (userRepository.existsByUsername(dto.getUsername())) {
				throw new AppException(AppException.VALIDATION_ERROR,
						messages.get("error.user.username.taken", dto.getUsername()));
			}
			user.setUsername(dto.getUsername());
		}

		if (dto.getPassword() != null) {
			user.setPassword(passwordEncoder.encode(dto.getPassword()));
		}

		var saved = userRepository.save(user);
		log.info("Updated user {}", saved.getId());
		return modelMapper.map(saved, UserResponseDto.class);
	}

	public void softDelete(UUID id, UUID callerUserId) {
		var user = findOwnedUser(id, callerUserId);
		user.setEndDate(LocalDateTime.now());
		userRepository.save(user);
		log.info("Soft-deleted user {}", id);
	}

	private User findOwnedUser(UUID id, UUID callerUserId) {
		if (!id.equals(callerUserId)) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.user.forbidden"));
		}
		return userRepository.findById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.user.not.found")));
	}
}
