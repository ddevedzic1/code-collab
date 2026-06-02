package com.codecollab.execution_service.execution.worker;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionWorker {

	private static final int SUCCESS_PERCENT = 80;
	private static final int MIN_DURATION_MS = 500;
	private static final int MAX_DURATION_MS = 2000;

	private final ExecutionPhaseService phaseService;

	@RabbitListener(queues = "${app.rabbitmq.execution.queue}")
	public void process(ExecutionMessage message) {
		var executionId = message.executionId();
		log.info("Worker picked up execution {}", executionId);
		try {
			if (!phaseService.markRunning(executionId)) {
				return;
			}
			runMockedExecution(executionId);
		} catch (Exception ex) {
			log.error("Worker failed processing execution {}", executionId, ex);
			phaseService.markFailed(executionId, ex.getMessage());
		}
	}

	private void runMockedExecution(UUID executionId) throws InterruptedException {
		var start = System.currentTimeMillis();
		var sleepMs = ThreadLocalRandom.current().nextInt(MIN_DURATION_MS, MAX_DURATION_MS + 1);
		Thread.sleep(sleepMs);
		var success = ThreadLocalRandom.current().nextInt(100) < SUCCESS_PERCENT;
		var durationMs = (int) (System.currentTimeMillis() - start);

		if (success) {
			phaseService.markCompleted(executionId, "Hello from mocked execution!\n", null, 0, durationMs);
		} else {
			phaseService.markCompleted(executionId, null, "Mock failure: simulated runtime error\n", 1, durationMs);
		}
		log.info("Finished execution {} ({} ms)", executionId, durationMs);
	}
}
