package com.codecollab.user_service.user.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.service.BaseService;
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

	public UserResponseDto getById(UUID id) {
		var user = findActiveById(id);
		return modelMapper.map(user, UserResponseDto.class);
	}

	public UserResponseDto update(UUID id, UserUpdateDto dto) {
		var user = findActiveById(id);

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

	public void softDelete(UUID id) {
		var user = findActiveById(id);
		user.setEndDate(LocalDateTime.now());
		userRepository.save(user);
		log.info("Soft-deleted user {}", id);
	}

	private User findActiveById(UUID id) {
		return userRepository.findById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.user.not.found")));
	}
}
