package com.codecollab.snippet_service.share.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.snippet_service.client.user.UserClient;
import com.codecollab.snippet_service.client.user.UserLookupClientDto;
import com.codecollab.snippet_service.exception.AppException;
import com.codecollab.snippet_service.language.model.Language;
import com.codecollab.snippet_service.share.dto.ShareCreateDto;
import com.codecollab.snippet_service.share.dto.ShareUserCreateDto;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;
import com.codecollab.snippet_service.share.model.SnippetShare;
import com.codecollab.snippet_service.share.model.SnippetShareUser;
import com.codecollab.snippet_service.share.repository.SnippetShareRepository;
import com.codecollab.snippet_service.share.repository.SnippetShareUserRepository;
import com.codecollab.snippet_service.snippet.model.Snippet;
import com.codecollab.snippet_service.snippet.repository.SnippetRepository;
import com.codecollab.snippet_service.util.Messages;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.RequestTemplate;

@ExtendWith(MockitoExtension.class)
class ShareServiceTest {

	@Mock
	private SnippetShareRepository snippetShareRepository;

	@Mock
	private SnippetShareUserRepository snippetShareUserRepository;

	@Mock
	private SnippetRepository snippetRepository;

	@Mock
	private UserClient userClient;

	@Mock
	private Messages messages;

	@InjectMocks
	private ShareService shareService;

	private final UUID ownerId = UUID.randomUUID();
	private final UUID otherUserId = UUID.randomUUID();
	private final UUID snippetId = UUID.randomUUID();
	private final String token = "token-123";

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(shareService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(shareService, "messages", messages);
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private Language language() {
		var language = new Language();
		language.setId(UUID.randomUUID());
		language.setCode("python");
		language.setName("Python");
		language.setVersion("3.11");
		language.setRuntimeImage("python:3.11-slim");
		return language;
	}

	private Snippet snippet() {
		var snippet = new Snippet();
		snippet.setId(snippetId);
		snippet.setUserId(ownerId);
		snippet.setLanguage(language());
		snippet.setTitle("Shared title");
		snippet.setContent("print('shared')");
		return snippet;
	}

	private SnippetShare share(ShareType type, Permission permission) {
		var share = new SnippetShare();
		share.setId(UUID.randomUUID());
		share.setSnippet(snippet());
		share.setShareToken(token);
		share.setShareType(type);
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

	@Test
	void getByToken_throwsNotFound_whenTokenInvalid() {
		when(snippetShareRepository.findByShareToken(token)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> shareService.getByToken(token, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void getByToken_grantsEditToOwner_regardlessOfShareSettings() {
		when(snippetShareRepository.findByShareToken(token))
				.thenReturn(Optional.of(share(ShareType.USER, Permission.READ_ONLY)));

		var result = shareService.getByToken(token, ownerId);

		assertThat(result.getPermission()).isEqualTo(Permission.EDIT);
		assertThat(result.getSnippetId()).isEqualTo(snippetId);
		assertThat(result.getTitle()).isEqualTo("Shared title");
		verify(snippetShareUserRepository, never()).findBySnippetShareIdAndUserId(any(), any());
	}

	@Test
	void getByToken_publicLinkAnonymous_usesShareReadOnlyPermission() {
		when(snippetShareRepository.findByShareToken(token))
				.thenReturn(Optional.of(share(ShareType.PUBLIC_LINK, Permission.READ_ONLY)));

		var result = shareService.getByToken(token, null);

		assertThat(result.getPermission()).isEqualTo(Permission.READ_ONLY);
	}

	@Test
	void getByToken_publicLinkAnonymous_usesShareEditPermission() {
		when(snippetShareRepository.findByShareToken(token))
				.thenReturn(Optional.of(share(ShareType.PUBLIC_LINK, Permission.EDIT)));

		var result = shareService.getByToken(token, null);

		assertThat(result.getPermission()).isEqualTo(Permission.EDIT);
	}

	@Test
	void getByToken_userShareOnList_usesPerUserEditPermission() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		when(snippetShareRepository.findByShareToken(token)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.of(shareUser(otherUserId, Permission.EDIT)));

		var result = shareService.getByToken(token, otherUserId);

		assertThat(result.getPermission()).isEqualTo(Permission.EDIT);
	}

	@Test
	void getByToken_userShareOnList_usesPerUserReadOnlyPermission() {
		var share = share(ShareType.USER, Permission.EDIT);
		when(snippetShareRepository.findByShareToken(token)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.of(shareUser(otherUserId, Permission.READ_ONLY)));

		var result = shareService.getByToken(token, otherUserId);

		assertThat(result.getPermission()).isEqualTo(Permission.READ_ONLY);
	}

	@Test
	void getByToken_userShareNotOnList_throwsForbidden() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		when(snippetShareRepository.findByShareToken(token)).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(Optional.empty());

		assertThatThrownBy(() -> shareService.getByToken(token, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void getByToken_userShareAnonymous_throwsForbidden() {
		when(snippetShareRepository.findByShareToken(token))
				.thenReturn(Optional.of(share(ShareType.USER, Permission.READ_ONLY)));

		assertThatThrownBy(() -> shareService.getByToken(token, null))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void create_throwsForbidden_whenCallerIsNotOwner() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.PUBLIC_LINK);
		dto.setPermission(Permission.READ_ONLY);
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet()));

		assertThatThrownBy(() -> shareService.create(snippetId, dto, otherUserId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void create_throwsValidation_whenShareAlreadyExists() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.PUBLIC_LINK);
		dto.setPermission(Permission.READ_ONLY);
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet()));
		when(snippetShareRepository.existsBySnippetId(snippetId)).thenReturn(true);

		assertThatThrownBy(() -> shareService.create(snippetId, dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void create_defaultsPermissionToReadOnly_whenNullForUserShare() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.USER);
		dto.setPermission(null);
		when(snippetRepository.findActiveById(snippetId)).thenReturn(Optional.of(snippet()));
		when(snippetShareRepository.existsBySnippetId(snippetId)).thenReturn(false);
		when(snippetShareRepository.save(any(SnippetShare.class))).thenAnswer(call -> call.getArgument(0));

		var result = shareService.create(snippetId, dto, ownerId);

		assertThat(result.getPermission()).isEqualTo(Permission.READ_ONLY);
		assertThat(result.getShareType()).isEqualTo(ShareType.USER);
		assertThat(result.getShareToken()).isNotBlank();
	}

	@Test
	void addUser_throwsValidation_whenAddingSelf() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		var dto = new ShareUserCreateDto();
		dto.setUsername("owner");
		dto.setPermission(Permission.EDIT);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		var self = new UserLookupClientDto();
		self.setId(ownerId);
		self.setUsername("owner");
		when(userClient.lookupByUsername("owner")).thenReturn(self);

		assertThatThrownBy(() -> shareService.addUser(share.getId(), dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void addUser_throwsValidation_whenUserAlreadyAdded() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		var dto = new ShareUserCreateDto();
		dto.setUsername("bob");
		dto.setPermission(Permission.EDIT);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		var target = new UserLookupClientDto();
		target.setId(otherUserId);
		target.setUsername("bob");
		when(userClient.lookupByUsername("bob")).thenReturn(target);
		when(snippetShareUserRepository.existsBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(true);

		assertThatThrownBy(() -> shareService.addUser(share.getId(), dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void addUser_succeeds_andReturnsUsername() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		var dto = new ShareUserCreateDto();
		dto.setUsername("bob");
		dto.setPermission(Permission.EDIT);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		var target = new UserLookupClientDto();
		target.setId(otherUserId);
		target.setUsername("bob");
		when(userClient.lookupByUsername("bob")).thenReturn(target);
		when(snippetShareUserRepository.existsBySnippetShareIdAndUserId(share.getId(), otherUserId))
				.thenReturn(false);
		when(snippetShareUserRepository.save(any(SnippetShareUser.class)))
				.thenAnswer(call -> call.getArgument(0));

		var result = shareService.addUser(share.getId(), dto, ownerId);

		assertThat(result.getUsername()).isEqualTo("bob");
		assertThat(result.getUserId()).isEqualTo(otherUserId);
		assertThat(result.getPermission()).isEqualTo(Permission.EDIT);
	}

	@Test
	void addUser_throwsValidation_whenUsernameNotFound() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		var dto = new ShareUserCreateDto();
		dto.setUsername("ghost");
		dto.setPermission(Permission.EDIT);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		when(userClient.lookupByUsername("ghost")).thenThrow(notFound());

		assertThatThrownBy(() -> shareService.addUser(share.getId(), dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void addUser_throwsServiceUnavailable_whenUserServiceFails() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		var dto = new ShareUserCreateDto();
		dto.setUsername("bob");
		dto.setPermission(Permission.EDIT);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		when(userClient.lookupByUsername("bob")).thenThrow(new RuntimeException("connection refused"));

		assertThatThrownBy(() -> shareService.addUser(share.getId(), dto, ownerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.SERVICE_UNAVAILABLE_ERROR);
	}

	@Test
	void getUsers_enrichesUsernames_viaBatchLookup() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareId(share.getId()))
				.thenReturn(List.of(shareUser(otherUserId, Permission.EDIT)));
		var looked = new UserLookupClientDto();
		looked.setId(otherUserId);
		looked.setUsername("bob");
		when(userClient.lookupByIds(anyList())).thenReturn(List.of(looked));

		var result = shareService.getUsers(share.getId(), ownerId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUsername()).isEqualTo("bob");
	}

	@Test
	void getUsers_degradesGracefully_whenLookupFails() {
		var share = share(ShareType.USER, Permission.READ_ONLY);
		when(snippetShareRepository.findById(share.getId())).thenReturn(Optional.of(share));
		when(snippetShareUserRepository.findBySnippetShareId(share.getId()))
				.thenReturn(List.of(shareUser(otherUserId, Permission.EDIT)));
		when(userClient.lookupByIds(anyList())).thenThrow(new RuntimeException("down"));

		var result = shareService.getUsers(share.getId(), ownerId);

		assertThat(result).hasSize(1);
		assertThat(result.get(0).getUsername()).isNull();
	}

	private FeignException.NotFound notFound() {
		var request = Request.create(HttpMethod.GET, "/api/v1/users/by-username/ghost",
				java.util.Map.of(), null, new RequestTemplate());
		return new FeignException.NotFound("Not Found", request, null, null);
	}
}
