package com.codecollab.execution_service.execution.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import com.codecollab.execution_service.client.snippet.LanguageClientDto;
import com.codecollab.execution_service.client.snippet.SnippetClient;
import com.codecollab.execution_service.client.snippet.SnippetClientDto;
import com.codecollab.execution_service.exception.AppException;
import com.codecollab.execution_service.execution.dto.ExecutionSubmitDto;
import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionQueue;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.model.QueueStatus;
import com.codecollab.execution_service.execution.repository.ExecutionQueueRepository;
import com.codecollab.execution_service.execution.repository.ExecutionRepository;
import com.codecollab.execution_service.util.Messages;

import feign.FeignException;
import feign.Request;
import feign.Request.HttpMethod;
import feign.RequestTemplate;

@ExtendWith(MockitoExtension.class)
class ExecutionServiceTest {

	@Mock
	private ExecutionRepository executionRepository;

	@Mock
	private ExecutionQueueRepository executionQueueRepository;

	@Mock
	private RabbitTemplate rabbitTemplate;

	@Mock
	private SnippetClient snippetClient;

	@Mock
	private Messages messages;

	@InjectMocks
	private ExecutionService executionService;

	private final UUID callerId = UUID.randomUUID();
	private final UUID snippetId = UUID.randomUUID();
	private final UUID languageId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		var modelMapper = new ModelMapper();
		modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		ReflectionTestUtils.setField(executionService, "modelMapper", modelMapper);
		ReflectionTestUtils.setField(executionService, "messages", messages);
		ReflectionTestUtils.setField(executionService, "exchangeName", "execution.exchange");
		ReflectionTestUtils.setField(executionService, "routingKey", "execution.submit");
		lenient().when(messages.get(anyString())).thenReturn("message");
		lenient().when(messages.get(anyString(), any())).thenReturn("message");
	}

	private SnippetClientDto snippetDto(String runtimeImage) {
		var language = new LanguageClientDto();
		language.setId(languageId);
		language.setRuntimeImage(runtimeImage);
		var snippet = new SnippetClientDto();
		snippet.setId(snippetId);
		snippet.setUserId(callerId);
		snippet.setLanguage(language);
		snippet.setContent("print('hi')");
		return snippet;
	}

	private ExecutionSubmitDto submitDto() {
		var dto = new ExecutionSubmitDto();
		dto.setSnippetId(snippetId);
		return dto;
	}

	@Test
	void submit_createsPendingExecution_andQueuesIt() {
		when(snippetClient.getById(snippetId, callerId)).thenReturn(snippetDto("python:3.11-slim"));
		when(executionRepository.save(any(Execution.class))).thenAnswer(call -> {
			Execution e = call.getArgument(0);
			e.setId(UUID.randomUUID());
			return e;
		});

		var result = executionService.submit(submitDto(), callerId);

		assertThat(result.getStatus()).isEqualTo(ExecutionStatus.PENDING);
		var executionCaptor = ArgumentCaptor.forClass(Execution.class);
		verify(executionRepository).save(executionCaptor.capture());
		var saved = executionCaptor.getValue();
		assertThat(saved.getUserId()).isEqualTo(callerId);
		assertThat(saved.getCodeSnapshot()).isEqualTo("print('hi')");
		assertThat(saved.getRuntimeImage()).isEqualTo("python:3.11-slim");

		var queueCaptor = ArgumentCaptor.forClass(ExecutionQueue.class);
		verify(executionQueueRepository).save(queueCaptor.capture());
		assertThat(queueCaptor.getValue().getStatus()).isEqualTo(QueueStatus.WAITING);
	}

	@Test
	void submit_publishesMessageAfterPersist() {
		when(snippetClient.getById(snippetId, callerId)).thenReturn(snippetDto("python:3.11-slim"));
		when(executionRepository.save(any(Execution.class))).thenAnswer(call -> {
			Execution e = call.getArgument(0);
			e.setId(UUID.randomUUID());
			return e;
		});

		executionService.submit(submitDto(), callerId);

		verify(rabbitTemplate).convertAndSend(eq("execution.exchange"), eq("execution.submit"), any(Object.class));
	}

	@Test
	void submit_throwsValidation_whenRuntimeImageMissing() {
		when(snippetClient.getById(snippetId, callerId)).thenReturn(snippetDto(null));

		assertThatThrownBy(() -> executionService.submit(submitDto(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
		verify(executionRepository, never()).save(any());
	}

	@Test
	void submit_throwsValidation_whenRuntimeImageBlank() {
		when(snippetClient.getById(snippetId, callerId)).thenReturn(snippetDto("  "));

		assertThatThrownBy(() -> executionService.submit(submitDto(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.VALIDATION_ERROR);
	}

	@Test
	void submit_mapsSnippetNotFoundToNotFound() {
		when(snippetClient.getById(snippetId, callerId)).thenThrow(feign(404));

		assertThatThrownBy(() -> executionService.submit(submitDto(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void submit_mapsSnippetForbiddenToForbidden() {
		when(snippetClient.getById(snippetId, callerId)).thenThrow(feign(403));

		assertThatThrownBy(() -> executionService.submit(submitDto(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	@Test
	void submit_mapsOtherFeignErrorsToServiceUnavailable() {
		when(snippetClient.getById(snippetId, callerId)).thenThrow(feign(500));

		assertThatThrownBy(() -> executionService.submit(submitDto(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.SERVICE_UNAVAILABLE_ERROR);
	}

	@Test
	void getById_returnsDto_whenCallerIsOwner() {
		var execution = new Execution();
		execution.setId(UUID.randomUUID());
		execution.setUserId(callerId);
		execution.setSnippetId(snippetId);
		execution.setStatus(ExecutionStatus.COMPLETED);
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));

		var result = executionService.getById(execution.getId(), callerId);

		assertThat(result.getStatus()).isEqualTo(ExecutionStatus.COMPLETED);
	}

	@Test
	void getById_throwsNotFound_whenMissing() {
		var id = UUID.randomUUID();
		when(executionRepository.findById(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> executionService.getById(id, callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.NOT_FOUND_ERROR);
	}

	@Test
	void getById_throwsForbidden_whenCallerIsNotOwner() {
		var execution = new Execution();
		execution.setId(UUID.randomUUID());
		execution.setUserId(UUID.randomUUID());
		when(executionRepository.findById(execution.getId())).thenReturn(Optional.of(execution));

		assertThatThrownBy(() -> executionService.getById(execution.getId(), callerId))
				.isInstanceOf(AppException.class)
				.extracting("code").isEqualTo(AppException.FORBIDDEN_ERROR);
	}

	private FeignException feign(int status) {
		var request = Request.create(HttpMethod.GET, "/api/v1/snippets/x",
				java.util.Map.of(), null, new RequestTemplate());
		return switch (status) {
			case 404 -> new FeignException.NotFound("Not Found", request, null, null);
			case 403 -> new FeignException.Forbidden("Forbidden", request, null, null);
			default -> new FeignException.InternalServerError("Server Error", request, null, null);
		};
	}
}
