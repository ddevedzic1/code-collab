package com.codecollab.user_service.auth.service;

import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.codecollab.user_service.auth.dto.LoginRequestDto;
import com.codecollab.user_service.auth.dto.RegisterRequestDto;
import com.codecollab.user_service.auth.dto.SessionUserDto;
import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.service.BaseService;
import com.codecollab.user_service.user.dto.UserResponseDto;
import com.codecollab.user_service.user.model.User;
import com.codecollab.user_service.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends BaseService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserResponseDto register(RegisterRequestDto dto) {
		var normalizedEmail = dto.getEmail().toLowerCase();

		if (userRepository.existsByUsername(dto.getUsername())) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.user.username.taken", dto.getUsername()));
		}
		if (userRepository.existsByEmail(normalizedEmail)) {
			throw new AppException(AppException.VALIDATION_ERROR,
					messages.get("error.user.email.taken", normalizedEmail));
		}

		var user = new User();
		user.setUsername(dto.getUsername());
		user.setEmail(normalizedEmail);
		user.setPassword(passwordEncoder.encode(dto.getPassword()));

		var saved = userRepository.save(user);
		log.info("Registered new user {} with username {}", saved.getId(), saved.getUsername());
		return modelMapper.map(saved, UserResponseDto.class);
	}

	public SessionUserDto authenticate(LoginRequestDto dto) {
		var user = userRepository.findByUsername(dto.getUsername())
				.filter(candidate -> passwordEncoder.matches(dto.getPassword(), candidate.getPassword()))
				.orElseThrow(() -> new AppException(AppException.UNAUTHORIZED_ERROR,
						messages.get("error.auth.invalid.credentials")));

		log.info("User {} authenticated successfully", user.getId());
		return toSessionUser(user);
	}

	public SessionUserDto getActiveUser(UUID userId) {
		var user = userRepository.findById(userId)
				.orElseThrow(() -> new AppException(AppException.UNAUTHORIZED_ERROR,
						messages.get("error.auth.session.invalid")));
		return toSessionUser(user);
	}

	private SessionUserDto toSessionUser(User user) {
		var dto = new SessionUserDto();
		dto.setId(user.getId());
		dto.setUsername(user.getUsername());
		return dto;
	}
}
