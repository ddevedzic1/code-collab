package com.codecollab.snippet_service.snippet.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.language.repository.LanguageRepository;
import com.codecollab.snippet_service.snippet.dto.SnippetCreateDto;
import com.codecollab.snippet_service.snippet.dto.SnippetUpdateDto;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.snippet.repository.SnippetRepository;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.model.SnippetShare;
import com.codecollab.snippet_service.share.model.SnippetShareUser;
import com.codecollab.snippet_service.share.repository.SnippetShareRepository;
import com.codecollab.snippet_service.share.repository.SnippetShareUserRepository;
import com.codecollab.snippet_service.util.Messages;

@ExtendWith(MockitoExtension.class)
class SnippetServiceTest {

	@Mock
	private SnippetRepository snippetRepository;

	@Mock
	private LanguageRepository languageRepository;

	@Mock
	private SnippetShareRepository snippetShareRepository;

	@Mock
	private SnippetShareUserRepository snippetShareUserRepository;

	@Mock
	private Messages messages;

	@InjectMocks
	private SnippetService snippetService;

	private final UUID ownerId = UUID.randomUUID();
	private final UUID otherUserId = UUID.randomUUID();
	private final UUID snippetId = UUID.randomUUID();
	private final UUID languageId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(snippetService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(snippetService, "messages", messages);
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private Language language() {
		var language = new Language();
		language.setId(languageId);
		language.setCode("python");
		language.setName("Python");
		language.setVersion("3.11");
		language.setRuntimeImage("python:3.11-slim");
		return language;
	}

	private Snippet snippet(UUID userId) {
		var snippet = new Snippet();
		snippet.setId(snippetId);
		snippet.setUserId(userId);
		snippet.setLanguage(language());
		snippet.setTitle("Title");
		snippet.setContent("print('hi')");
		return snippet;
	}

	@Test
	void getById_returnsDto_whenCallerIsOwner() {
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));

		var result = snippetService.getById(snippetId, ownerId);

		assertThat(result.getId()).isEqualTo(snippetId);
		assertThat(result.getUserId()).isEqualTo(ownerId);
		assertThat(result.getLanguage().getRuntimeImage()).isEqualTo("python:3.11-slim");
	}

	@Test
	void getById_throwsNotFound_whenSnippetMissing() {
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snippetService.getById(snippetId, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void getById_throwsForbidden_whenCallerIsNotOwner() {
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));

		assertThatThrownBy(() -> snippetService.getById(snippetId, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void create_persistsSnippetWithCallerAsOwner() {
		var dto = new SnippetCreateDto();
		dto.setLanguageId(languageId);
		dto.setTitle("New");
		dto.setContent("code");
		when(languageRepository.findById(languageId)).thenReturn(Optional.of(language()));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.create(dto, ownerId);

		assertThat(result.getUserId()).isEqualTo(ownerId);
		assertThat(result.getTitle()).isEqualTo("New");
		assertThat(result.getContent()).isEqualTo("code");
	}

	@Test
	void create_defaultsContentToEmpty_whenNull() {
		var dto = new SnippetCreateDto();
		dto.setLanguageId(languageId);
		dto.setTitle("New");
		dto.setContent(null);
		when(languageRepository.findById(languageId)).thenReturn(Optional.of(language()));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.create(dto, ownerId);

		assertThat(result.getContent()).isEmpty();
	}

	@Test
	void create_throwsValidation_whenLanguageMissing() {
		var dto = new SnippetCreateDto();
		dto.setLanguageId(languageId);
		dto.setTitle("New");
		when(languageRepository.findById(languageId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snippetService.create(dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void update_succeeds_whenCallerIsOwner() {
		var dto = new SnippetUpdateDto();
		dto.setTitle("Updated");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.update(snippetId, dto, ownerId);

		assertThat(result.getTitle()).isEqualTo("Updated");
		verify(snippetShareRepository, never()).findBySnippetId(any());
	}

	@Test
	void update_succeeds_whenUserShareGrantsEdit() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		var share = userShare();
		when(snippetShareRepository.findBySnippetId(snippetId)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.of(shareUser(otherUserId, Permission.EDIT)));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.update(snippetId, dto, otherUserId);

		assertThat(result.getContent()).isEqualTo("changed");
	}

	@Test
	void update_throwsForbidden_whenUserShareIsReadOnly() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		var share = userShare();
		when(snippetShareRepository.findBySnippetId(snippetId)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.of(shareUser(otherUserId, Permission.READ_ONLY)));

		assertThatThrownBy(() -> snippetService.update(snippetId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
		verify(snippetRepository, never()).save(any());
	}

	@Test
	void update_throwsForbidden_whenUserNotOnShareList() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		var share = userShare();
		when(snippetShareRepository.findBySnippetId(snippetId)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> snippetService.update(snippetId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void update_succeeds_whenPublicLinkShareGrantsEdit() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		when(snippetShareRepository.findBySnippetId(snippetId))
				.thenReturn(Optional.of(publicShare(Permission.EDIT)));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.update(snippetId, dto, otherUserId);

		assertThat(result.getContent()).isEqualTo("changed");
	}

	@Test
	void update_throwsForbidden_whenPublicLinkShareIsReadOnly() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		when(snippetShareRepository.findBySnippetId(snippetId))
				.thenReturn(Optional.of(publicShare(Permission.READ_ONLY)));

		assertThatThrownBy(() -> snippetService.update(snippetId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void update_throwsForbidden_whenNoShareExists() {
		var dto = new SnippetUpdateDto();
		dto.setContent("changed");
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		when(snippetShareRepository.findBySnippetId(snippetId)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> snippetService.update(snippetId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void update_changesLanguage_whenNewLanguageProvided() {
		var newLanguageId = UUID.randomUUID();
		var newLanguage = new Language();
		newLanguage.setId(newLanguageId);
		newLanguage.setCode("js");
		newLanguage.setName("JavaScript");
		newLanguage.setVersion("20");
		newLanguage.setRuntimeImage("node:20-slim");
		var dto = new SnippetUpdateDto();
		dto.setLanguageId(newLanguageId);
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));
		when(languageRepository.findById(newLanguageId)).thenReturn(Optional.of(newLanguage));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		var result = snippetService.update(snippetId, dto, ownerId);

		assertThat(result.getLanguage().getId()).isEqualTo(newLanguageId);
	}

	@Test
	void softDelete_setsEndDate_whenCallerIsOwner() {
		var snippet = snippet(ownerId);
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet));
		when(snippetRepository.save(any(Snippet.class))).thenAnswer(call -> call.getArgument(0));

		snippetService.softDelete(snippetId, ownerId);

		assertThat(snippet.getEndDate()).isNotNull();
		verify(snippetRepository).save(snippet);
	}

	@Test
	void softDelete_throwsForbidden_whenCallerIsNotOwner() {
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet(ownerId)));

		assertThatThrownBy(() -> snippetService.softDelete(snippetId, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	private SnippetShare userShare() {
		var share = new SnippetShare();
		share.setId(UUID.randomUUID());
		share.setShareType(ShareType.USER);
		share.setPermission(Permission.READ_ONLY);
		return share;
	}

	private SnippetShare publicShare(Permission permission) {
		var share = new SnippetShare();
		share.setId(UUID.randomUUID());
		share.setShareType(ShareType.PUBLIC_LINK);
		share.setPermission(permission);
		return share;
	}

	private SnippetShareUser shareUser(UUID userId, Permission permission) {
		var shareUser = new SnippetShareUser();
		shareUser.setId(UUID.randomUUID());
		shareUser.setUserId(userId);
		shareUser.setPermission(permission);
		return shareUser;
	}
}
