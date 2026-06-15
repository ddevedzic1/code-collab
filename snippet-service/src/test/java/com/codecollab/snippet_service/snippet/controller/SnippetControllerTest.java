package com.codecollab.snippet_service.snippet.controller;

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

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.snippet.dto.SnippetResponseDto;
import com.codecollab.snippet_service.snippet.service.SnippetService;
import com.codecollab.snippet_service.support.WebTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = SnippetController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebTestSupport.class)
class SnippetControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private SnippetService snippetService;

	private static final String USER_ID = UUID.randomUUID().toString();

	private SnippetResponseDto responseDto() {
		var dto = new SnippetResponseDto();
		dto.setId(UUID.randomUUID());
		dto.setUserId(UUID.fromString(USER_ID));
		dto.setTitle("Title");
		dto.setContent("code");
		return dto;
	}

	@Test
	void getById_returns200_withSnippet() throws Exception {
		var id = UUID.randomUUID();
		when(snippetService.getById(eq(id), any())).thenReturn(responseDto());

		mockMvc.perform(get("/api/v1/snippets/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Title"));
	}

	@Test
	void getById_returns403_whenForbidden() throws Exception {
		var id = UUID.randomUUID();
		when(snippetService.getById(eq(id), any()))
				.thenThrow(new AppException(AppException.FORBIDDEN_ERROR, "denied"));

		mockMvc.perform(get("/api/v1/snippets/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("forbidden"));
	}

	@Test
	void getById_returns404_whenMissing() throws Exception {
		var id = UUID.randomUUID();
		when(snippetService.getById(eq(id), any()))
				.thenThrow(new AppException(AppException.NOT_FOUND_ERROR, "missing"));

		mockMvc.perform(get("/api/v1/snippets/{id}", id).header("X-User-Id", USER_ID))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("not-found"));
	}

	@Test
	void getById_forwardsUserIdHeaderToService() throws Exception {
		var id = UUID.randomUUID();
		var caller = UUID.randomUUID();
		when(snippetService.getById(eq(id), eq(caller))).thenReturn(responseDto());

		mockMvc.perform(get("/api/v1/snippets/{id}", id).header("X-User-Id", caller.toString()))
				.andExpect(status().isOk());
	}

	@Test
	void create_returns201_whenValid() throws Exception {
		when(snippetService.create(any(), any())).thenReturn(responseDto());
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"languageId", UUID.randomUUID().toString(),
				"title", "Title"));

		mockMvc.perform(post("/api/v1/snippets")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isCreated());
	}

	@Test
	void create_returns400_whenTitleBlank() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"languageId", UUID.randomUUID().toString(),
				"title", ""));

		mockMvc.perform(post("/api/v1/snippets")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void create_returns400_whenLanguageIdMissing() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of("title", "Title"));

		mockMvc.perform(post("/api/v1/snippets")
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}
}
