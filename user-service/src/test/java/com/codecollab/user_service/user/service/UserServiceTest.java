package com.codecollab.user_service.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.user.dto.UserUpdateDto;
import com.codecollab.user_service.user.model.User;
import com.codecollab.user_service.user.repository.UserRepository;
import com.codecollab.user_service.util.Messages;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private Messages messages;

	@InjectMocks
	private UserService userService;

	private final UUID userId = UUID.randomUUID();
	private final UUID otherUserId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(userService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(userService, "messages", messages);
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private User user(UUID id, String username) {
		var user = new User();
		user.setId(id);
		user.setUsername(username);
		user.setEmail(username + "@example.com");
		user.setPassword("hashed");
		return user;
	}

	@Test
	void getById_returnsDto_whenCallerIsSelf() {
		when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "alice")));

		var result = userService.getById(userId, userId);

		assertThat(result.getUsername()).isEqualTo("alice");
	}

	@Test
	void getById_throwsForbidden_whenCallerIsOther() {
		assertThatThrownBy(() -> userService.getById(userId, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
		verify(userRepository, never()).findById(any());
	}

	@Test
	void getById_throwsNotFound_whenUserMissing() {
		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.getById(userId, userId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void lookupByUsername_returnsDto_whenFound() {
		when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user(userId, "alice")));

		var result = userService.lookupByUsername("alice");

		assertThat(result.getId()).isEqualTo(userId);
		assertThat(result.getUsername()).isEqualTo("alice");
	}

	@Test
	void lookupByUsername_throwsNotFound_whenMissing() {
		when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

		assertThatThrownBy(() -> userService.lookupByUsername("ghost"))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void lookupByIds_returnsEmpty_whenIdsEmpty() {
		assertThat(userService.lookupByIds(List.of())).isEmpty();
		verify(userRepository, never()).findAllById(any());
	}

	@Test
	void lookupByIds_returnsEmpty_whenIdsNull() {
		assertThat(userService.lookupByIds(null)).isEmpty();
	}

	@Test
	void lookupByIds_mapsFoundUsers() {
		when(userRepository.findAllById(List.of(userId)))
				.thenReturn(List.of(user(userId, "alice")));

		var result = userService.lookupByIds(List.of(userId));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUsername()).isEqualTo("alice");
	}

	@Test
	void update_throwsForbidden_whenCallerIsOther() {
		var dto = new UserUpdateDto();
		dto.setUsername("newname");

		assertThatThrownBy(() -> userService.update(userId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void update_changesUsername_whenFree() {
		var dto = new UserUpdateDto();
		dto.setUsername("newname");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "alice")));
		when(userRepository.existsByUsername("newname")).thenReturn(false);
		when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

		var result = userService.update(userId, dto, userId);

		assertThat(result.getUsername()).isEqualTo("newname");
	}

	@Test
	void update_throwsValidation_whenNewUsernameTaken() {
		var dto = new UserUpdateDto();
		dto.setUsername("taken");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "alice")));
		when(userRepository.existsByUsername("taken")).thenReturn(true);

		assertThatThrownBy(() -> userService.update(userId, dto, userId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void update_encodesPassword_whenProvided() {
		var dto = new UserUpdateDto();
		dto.setPassword("newpassword");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "alice")));
		when(passwordEncoder.encode("newpassword")).thenReturn("newhash");
		when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

		userService.update(userId, dto, userId);

		verify(passwordEncoder).encode("newpassword");
	}

	@Test
	void update_skipsUsernameCheck_whenUnchanged() {
		var dto = new UserUpdateDto();
		dto.setUsername("alice");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user(userId, "alice")));
		when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

		userService.update(userId, dto, userId);

		verify(userRepository, never()).existsByUsername(any());
	}

	@Test
	void softDelete_setsEndDate_whenCallerIsSelf() {
		var user = user(userId, "alice");
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

		userService.softDelete(userId, userId);

		assertThat(user.getEndDate()).isNotNull();
	}

	@Test
	void softDelete_throwsForbidden_whenCallerIsOther() {
		assertThatThrownBy(() -> userService.softDelete(userId, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}
}
