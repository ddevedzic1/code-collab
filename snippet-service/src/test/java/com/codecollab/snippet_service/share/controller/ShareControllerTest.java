package com.codecollab.snippet_service.share.controller;

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
import com.codecollab.snippet_service.share.dto.SharedSnippetResponseDto;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.service.ShareService;
import com.codecollab.snippet_service.support.WebTestSupport;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ShareController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(WebTestSupport.class)
class ShareControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ShareService shareService;

	private static final String USER_ID = UUID.randomUUID().toString();

	@Test
	void getByToken_returns200_forAnonymousCaller() throws Exception {
		var dto = new SharedSnippetResponseDto();
		dto.setSnippetId(UUID.randomUUID());
		dto.setTitle("Shared");
		dto.setPermission(Permission.READ_ONLY);
		when(shareService.getByToken(eq("tok"), any())).thenReturn(dto);

		mockMvc.perform(get("/api/v1/shares/by-token/{token}", "tok"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.permission").value("READ_ONLY"));
	}

	@Test
	void getByToken_returns403_whenAccessDenied() throws Exception {
		when(shareService.getByToken(eq("tok"), any()))
				.thenThrow(new AppException(AppException.FORBIDDEN_ERROR, "denied"));

		mockMvc.perform(get("/api/v1/shares/by-token/{token}", "tok"))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("forbidden"));
	}

	@Test
	void addUser_returns400_whenUsernameBlank() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of(
				"username", "",
				"permission", "EDIT"));

		mockMvc.perform(post("/api/v1/shares/{shareId}/users", UUID.randomUUID())
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void addUser_returns400_whenPermissionMissing() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of("username", "bob"));

		mockMvc.perform(post("/api/v1/shares/{shareId}/users", UUID.randomUUID())
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}

	@Test
	void createShare_returns400_whenPublicLinkMissingPermission() throws Exception {
		var body = objectMapper.writeValueAsString(java.util.Map.of("shareType", "PUBLIC_LINK"));

		mockMvc.perform(post("/api/v1/snippets/{snippetId}/share", UUID.randomUUID())
				.header("X-User-Id", USER_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(body))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("validation-error"));
	}
}
