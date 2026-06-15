package com.codecollab.user_service.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.user_service.auth.dto.LoginRequestDto;
import com.codecollab.user_service.auth.dto.RegisterRequestDto;
import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.user.model.User;
import com.codecollab.user_service.user.repository.UserRepository;
import com.codecollab.user_service.util.Messages;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private Messages messages;

	@InjectMocks
	private AuthService authService;

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(authService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(authService, "messages", messages);
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private User user(UUID id, String username, String hashedPassword) {
		var user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setEmail(username + "@example.com");
		user.setPassword(hashedPassword);
		return user;
	}

	@Test
	void register_persistsUser_andEncodesPassword() {
		var dto = new RegisterRequestDto();
		dto.setUsername("alice");
		dto.setEmail("Alice@Example.COM");
		dto.setPassword("plaintext");
		when(userRepository.existsByUsername("alice")).thenReturn(false);
		when(userRepository.existsByEmail("alice@example.com")).thenReturn(false);
		when(passwordEncoder.encode("plaintext")).thenReturn("hashed");
		when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

		var result = authService.register(dto);

		var captor = ArgumentCaptor.forClass(User.class);
		verify(userRepository).save(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("alice@example.com");
		assertThat(captor.getValue().getPassword()).isEqualTo("hashed");
		assertThat(result.getUsername()).isEqualTo("alice");
	}

	@Test
	void register_throwsValidation_whenUsernameTaken() {
		var dto = new RegisterRequestDto();
		dto.setUsername("alice");
		dto.setEmail("alice@example.com");
		dto.setPassword("plaintext");
		when(userRepository.existsByUsername("alice")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
		verify(userRepository, never()).save(any());
	}

	@Test
	void register_throwsValidation_whenEmailTaken() {
		var dto = new RegisterRequestDto();
		dto.setUsername("alice");
		dto.setEmail("alice@example.com");
		dto.setPassword("plaintext");
		when(userRepository.existsByUsername("alice")).thenReturn(false);
		when(userRepository.existsByEmail("alice@example.com")).thenReturn(true);

		assertThatThrownBy(() -> authService.register(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void authenticate_returnsSession_whenCredentialsValid() {
		var id = UUID.randomUUID();
		var dto = new LoginRequestDto();
		dto.setUsername("alice");
		dto.setPassword("plaintext");
		when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user(id, "alice", "hashed")));
		when(passwordEncoder.matches("plaintext", "hashed")).thenReturn(true);

		var result = authService.authenticate(dto);

		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getUsername()).isEqualTo("alice");
	}

	@Test
	void authenticate_throwsUnauthorized_whenUsernameUnknown() {
		var dto = new LoginRequestDto();
		dto.setUsername("ghost");
		dto.setPassword("plaintext");
		when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.authenticate(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.UNAUTHORIZED_ERROR);
	}

	@Test
	void authenticate_throwsUnauthorized_whenPasswordWrong() {
		var dto = new LoginRequestDto();
		dto.setUsername("alice");
		dto.setPassword("wrong");
		when(userRepository.findByUsername("alice"))
				.thenReturn(Optional.of(user(UUID.randomUUID(), "alice", "hashed")));
		when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

		assertThatThrownBy(() -> authService.authenticate(dto))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.UNAUTHORIZED_ERROR);
	}

	@Test
	void getActiveUser_returnsSession_whenFound() {
		var id = UUID.randomUUID();
		when(userRepository.findById(id)).thenReturn(Optional.of(user(id, "alice", "hashed")));

		var result = authService.getActiveUser(id);

		assertThat(result.getId()).isEqualTo(id);
	}

	@Test
	void getActiveUser_throwsUnauthorized_whenMissing() {
		var id = UUID.randomUUID();
		when(userRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> authService.getActiveUser(id))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.UNAUTHORIZED_ERROR);
	}
}
