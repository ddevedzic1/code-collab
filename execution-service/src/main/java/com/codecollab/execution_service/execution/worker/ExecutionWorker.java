package com.codecollab.execution_service.execution.worker;

import java.util.UUID;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionWorker {

	private final ExecutionPhaseService phaseService;
	private final DockerExecutionRunner dockerExecutionRunner;

	@RabbitListener(queues = "${app.rabbitmq.execution.queue}")
	public void process(ExecutionMessage message) {
		var executionId = message.executionId();
		log.info("Worker picked up execution {}", executionId);
		try {
			var context = phaseService.markRunning(executionId);
			if (context == null) {
				return;
			}
			runDockerExecution(executionId, context);
		} catch (Exception ex) {
			log.error("Worker failed processing execution {}", executionId, ex);
			phaseService.markFailed(executionId, "Execution could not be completed due to an internal error.");
		}
	}

	private void runDockerExecution(UUID executionId, ExecutionContext context) {
		if (context.runtimeImage() == null || context.runtimeImage().isBlank()) {
			log.warn("Execution {} has no runtime image; marking failed", executionId);
			phaseService.markFailed(executionId, "No runtime image was configured for this execution.");
			return;
		}

		var result = dockerExecutionRunner.run(executionId, context.runtimeImage(), context.codeSnapshot());
		phaseService.markCompleted(executionId, result.stdout(), result.stderr(),
				result.exitCode(), result.durationMs());
		log.info("Finished execution {} (exit {}, {} ms, timedOut={})",
				executionId, result.exitCode(), result.durationMs(), result.timedOut());
	}
}
