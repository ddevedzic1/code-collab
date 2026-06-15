package com.codecollab.execution_service.execution.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

import com.codecollab.execution_service.exception.AppException;
import com.codecollab.execution_service.execution.dto.ExecutionResponseDto;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.service.ExecutionService;
import com.codecollab.execution_service.support.WebTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ExecutionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebTestSupport.class)
class ExecutionControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ExecutionService executionService;

	private static final String USER_ID = UUID.randomUUID().toString();

	private ExecutionResponseDto responseDto(ExecutionStatus status) {
		var dto = new ExecutionResponseDto();
		dto.setId(UUID.randomUUID());
		dto.setUserId(UUID.fromString(USER_ID));
		dto.setStatus(status);
		return dto;
	}

	@Test
	void submit_returns202_whenAccepted() throws Exception {
		when(executionService.submit(any(), any())).thenReturn(responseDto(ExecutionStatus.PENDING));
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"snippetId", UUID.randomUUID().toString()));

		mockMvc.perform(post("/api/v1/executions")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isAccepted())
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	@Test
	void submit_returns400_whenSnippetIdMissing() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of());

		mockMvc.perform(post("/api/v1/executions")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void submit_returns404_whenSnippetNotFound() throws Exception {
		when(executionService.submit(any(), any()))
				.thenThrow(new AppException(AppException.NOT_FOUND_ERROR, "missing"));
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"snippetId", UUID.randomUUID().toString()));

		mockMvc.perform(post("/api/v1/executions")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isNotFound());
	}

	@Test
	void submit_returns503_whenDownstreamUnavailable() throws Exception {
		when(executionService.submit(any(), any()))
				.thenThrow(new AppException(AppException.SERVICE_UNAVAILABLE_ERROR, "down"));
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"snippetId", UUID.randomUUID().toString()));

		mockMvc.perform(post("/api/v1/executions")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isServiceUnavailable());
	}

	@Test
	void getById_returns200() throws Exception {
		var id = UUID.randomUUID();
		when(executionService.getById(eq(id), any())).thenReturn(responseDto(ExecutionStatus.COMPLETED));

		mockMvc.perform(get("/api/v1/executions/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	@Test
	void getById_returns403_whenForbidden() throws Exception {
		var id = UUID.randomUUID();
		when(executionService.getById(eq(id), any()))
				.thenThrow(new AppException(AppException.FORBIDDEN_ERROR, "denied"));

		mockMvc.perform(get("/api/v1/executions/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isForbidden());
	}
}
