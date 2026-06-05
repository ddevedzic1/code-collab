package com.codecollab.system_events_service.event.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.codecollab.system_events_service.event.model.ActionType;
import com.codecollab.system_events_service.event.model.ResponseType;
import com.codecollab.system_events_service.event.model.SystemEvent;
import com.codecollab.system_events_service.event.repository.SystemEventRepository;
import com.codecollab.system_events_service.event.saga.ExecutionFinalizedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

	private static final String EXECUTION_MICROSERVICE = "execution-service";

	private final SystemEventRepository systemEventRepository;

	@Transactional
	public void recordExecutionFinalized(ExecutionFinalizedEvent event) {
		var resource = event.executionId().toString();

		if (systemEventRepository.existsByResourceAndActionType(resource, ActionType.EXECUTE)) {
			log.info("Audit for execution {} already recorded; treating as success (idempotent)", resource);
			return;
		}

		var systemEvent = new SystemEvent();
		systemEvent.setUserId(event.userId());
		systemEvent.setMicroservice(EXECUTION_MICROSERVICE);
		systemEvent.setActionType(ActionType.EXECUTE);
		systemEvent.setResource(resource);
		systemEvent.setResponseType("FAILED".equals(event.status()) ? ResponseType.ERROR : ResponseType.SUCCESS);
		systemEvent.setDetails(buildDetails(event));

		systemEventRepository.save(systemEvent);
		log.info("Recorded audit event for execution {} ({})", resource, event.status());
	}

	private String buildDetails(ExecutionFinalizedEvent event) {
		return String.format("status=%s; exitCode=%s; durationMs=%s; snippetId=%s",
				event.status(), event.exitCode(), event.durationMs(), event.snippetId());
	}
}
