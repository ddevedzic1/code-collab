package com.codecollab.user_service.auth.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.codecollab.user_service.auth.dto.SessionUserDto;
import com.codecollab.user_service.auth.service.AuthService;
import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.support.WebTestSupport;
import com.codecollab.user_service.user.dto.UserResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebTestSupport.class)
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private AuthService authService;

	@Test
	void register_returns201_whenValid() throws Exception {
		var dto = new UserResponseDto();
		dto.setId(UUID.randomUUID());
		dto.setUsername("alice");
		dto.setEmail("alice@example.com");
		when(authService.register(any())).thenReturn(dto);
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"email", "alice@example.com",
				"password", "password123"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void register_returns400_whenEmailInvalid() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"email", "not-an-email",
				"password", "password123"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void register_returns400_whenPasswordTooShort() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"email", "alice@example.com",
				"password", "short"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest());
	}

	@Test
	void register_returns400_whenUsernameTaken() throws Exception {
		when(authService.register(any()))
				.thenThrow(new AppException(AppException.VALIDATION_ERROR, "taken"));
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"email", "alice@example.com",
				"password", "password123"));

		mockMvc.perform(post("/api/v1/auth/register")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void login_returns200_andSessionUser() throws Exception {
		var sessionUser = new SessionUserDto();
		sessionUser.setId(UUID.randomUUID());
		sessionUser.setUsername("alice");
		when(authService.authenticate(any())).thenReturn(sessionUser);
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"password", "password123"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void login_returns401_whenCredentialsInvalid() throws Exception {
		when(authService.authenticate(any()))
				.thenThrow(new AppException(AppException.UNAUTHORIZED_ERROR, "invalid"));
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "alice",
				"password", "wrong-password"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.code").value("unauthorized"));
	}

	@Test
	void login_returns400_whenUsernameBlank() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "",
				"password", "password123"));

		mockMvc.perform(post("/api/v1/auth/login")
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest());
	}
}
