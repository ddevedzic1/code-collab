package com.codecollab.user_service.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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

import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.support.WebTestSupport;
import com.codecollab.user_service.user.dto.UserResponseDto;
import com.codecollab.user_service.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebTestSupport.class)
class UserControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private UserService userService;

	private static final String USER_ID = UUID.randomUUID().toString();

	private UserResponseDto responseDto() {
		var dto = new UserResponseDto();
		dto.setId(UUID.fromString(USER_ID));
		dto.setUsername("alice");
		dto.setEmail("alice@example.com");
		return dto;
	}

	@Test
	void getById_returns200() throws Exception {
		var id = UUID.fromString(USER_ID);
		when(userService.getById(eq(id), any())).thenReturn(responseDto());

		mockMvc.perform(get("/api/v1/users/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.username").value("alice"));
	}

	@Test
	void getById_returns403_whenForbidden() throws Exception {
		var id = UUID.randomUUID();
		when(userService.getById(eq(id), any()))
				.thenThrow(new AppException(AppException.FORBIDDEN_ERROR, "denied"));

		mockMvc.perform(get("/api/v1/users/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("forbidden"));
	}

	@Test
	void update_returns200_whenValid() throws Exception {
		var id = UUID.fromString(USER_ID);
		when(userService.update(eq(id), any(), any())).thenReturn(responseDto());
		var body = objectMapper.writeValueAsString(java.util.Map.of("username", "newname"));

		mockMvc.perform(patch("/api/v1/users/{id}", id)
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isOk());
	}

	@Test
	void update_returns400_whenUsernameTooShort() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of("username", "ab"));

		mockMvc.perform(patch("/api/v1/users/{id}", USER_ID)
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void update_returns400_whenPasswordTooShort() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of("password", "short"));

		mockMvc.perform(patch("/api/v1/users/{id}", USER_ID)
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void softDelete_returns204() throws Exception {
		var id = UUID.fromString(USER_ID);

		mockMvc.perform(delete("/api/v1/users/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isNoContent());
	}

	@Test
	void softDelete_returns403_whenForbidden() throws Exception {
		var id = UUID.randomUUID();
		doThrow(new AppException(AppException.FORBIDDEN_ERROR, "denied"))
				.when(userService).softDelete(eq(id), any());

		mockMvc.perform(delete("/api/v1/users/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isForbidden());
	}
}
