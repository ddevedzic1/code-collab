package com.codecollab.execution_service.execution.service;

import java.util.UUID;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.codecollab.execution_service.client.snippet.SnippetClient;
import com.codecollab.execution_service.client.snippet.SnippetClientDto;
import com.codecollab.execution_service.exception.AppException;
import com.codecollab.execution_service.execution.dto.ExecutionResponseDto;
import com.codecollab.execution_service.execution.dto.ExecutionSubmitDto;
import com.codecollab.execution_service.execution.model.AuditState;
import com.codecollab.execution_service.execution.model.Execution;
import com.codecollab.execution_service.execution.model.ExecutionQueue;
import com.codecollab.execution_service.execution.model.ExecutionStatus;
import com.codecollab.execution_service.execution.model.QueueStatus;
import com.codecollab.execution_service.execution.repository.ExecutionQueueRepository;
import com.codecollab.execution_service.execution.repository.ExecutionRepository;
import com.codecollab.execution_service.execution.worker.ExecutionMessage;
import com.codecollab.execution_service.service.BaseService;
import com.codecollab.execution_service.util.PageResult;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExecutionService extends BaseService {

	private final ExecutionRepository executionRepository;
	private final ExecutionQueueRepository executionQueueRepository;
	private final RabbitTemplate rabbitTemplate;
	private final SnippetClient snippetClient;

	@Value("${app.rabbitmq.execution.exchange}")
	private String exchangeName;

	@Value("${app.rabbitmq.execution.routing-key}")
	private String routingKey;

	@Transactional
	public ExecutionResponseDto submit(ExecutionSubmitDto dto, UUID callerUserId) {
		var snippet = fetchSnippet(dto.getSnippetId(), callerUserId);

		var execution = new Execution();
		execution.setUserId(callerUserId);
		execution.setSnippetId(snippet.getId());
		execution.setLanguageId(snippet.getLanguage().getId());
		execution.setCodeSnapshot(snippet.getContent());
		execution.setStatus(ExecutionStatus.PENDING);
		execution.setAuditState(AuditState.PENDING_AUDIT);
		var savedExecution = executionRepository.save(execution);

		var queueEntry = new ExecutionQueue();
		queueEntry.setExecution(savedExecution);
		queueEntry.setStatus(QueueStatus.WAITING);
		queueEntry.setPriority(0);
		executionQueueRepository.save(queueEntry);

		publishAfterCommit(new ExecutionMessage(savedExecution.getId()));

		log.info("Submitted execution {} for user {} snippet {}",
				savedExecution.getId(), savedExecution.getUserId(), savedExecution.getSnippetId());
		return modelMapper.map(savedExecution, ExecutionResponseDto.class);
	}

	@Transactional(readOnly = true)
	public ExecutionResponseDto getById(UUID id, UUID callerUserId) {
		var execution = executionRepository.findById(id)
				.orElseThrow(() -> new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.execution.not.found")));
		if (!execution.getUserId().equals(callerUserId)) {
			throw new AppException(AppException.FORBIDDEN_ERROR,
					messages.get("error.execution.forbidden"));
		}
		return modelMapper.map(execution, ExecutionResponseDto.class);
	}

	@Transactional(readOnly = true)
	public PageResult<ExecutionResponseDto> search(UUID callerUserId, UUID snippetId, ExecutionStatus status,
			Pageable pageable) {
		var page = executionRepository.search(callerUserId, snippetId, status, pageable)
				.map(execution -> modelMapper.map(execution, ExecutionResponseDto.class));
		return PageResult.from(page);
	}

	private SnippetClientDto fetchSnippet(UUID snippetId, UUID callerUserId) {
		try {
			return snippetClient.getById(snippetId, callerUserId);
		} catch (Exception ex) {
			var feign = unwrapFeign(ex);
			if (feign instanceof FeignException.NotFound) {
				throw new AppException(AppException.NOT_FOUND_ERROR,
						messages.get("error.execution.snippet.not.found"));
			}
			if (feign instanceof FeignException.Forbidden) {
				throw new AppException(AppException.FORBIDDEN_ERROR,
						messages.get("error.execution.snippet.forbidden"));
			}
			if (feign != null) {
				log.warn("Snippet lookup returned status {} for snippet {}", feign.status(), snippetId);
			} else {
				log.warn("Snippet lookup failed [{}] for snippet {}: {}",
						ex.getClass().getName(), snippetId, ex.getMessage());
			}
			throw new AppException(AppException.SERVICE_UNAVAILABLE_ERROR,
					messages.get("error.system.unavailable"));
		}
	}

	private static FeignException unwrapFeign(Throwable t) {
		if (t instanceof FeignException fe) {
			return fe;
		}
		if (t.getCause() instanceof FeignException fe) {
			return fe;
		}
		return null;
	}

	private void publishAfterCommit(ExecutionMessage message) {
		if (TransactionSynchronizationManager.isSynchronizationActive()) {
			TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
				@Override
				public void afterCommit() {
					tryPublish(message);
				}
			});
		} else {
			tryPublish(message);
		}
	}

	private void tryPublish(ExecutionMessage message) {
		try {
			rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
			log.debug("Published execution message {}", message.executionId());
		} catch (Exception ex) {
			log.error("Failed to publish execution message {} to RabbitMQ; "
					+ "execution remains PENDING and may need manual requeue",
					message.executionId(), ex);
		}
	}
}
