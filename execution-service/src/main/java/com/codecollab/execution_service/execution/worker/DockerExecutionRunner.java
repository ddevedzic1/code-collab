package com.codecollab.execution_service.execution.worker;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DockerExecutionRunner {

	private static final int DOCKER_DAEMON_ERROR_EXIT_CODE = 125;
	private static final int CONTAINER_CANNOT_INVOKE_EXIT_CODE = 126;
	private static final int CONTAINER_COMMAND_NOT_FOUND_EXIT_CODE = 127;

	@Value("${app.execution.docker.binary:docker}")
	private String dockerBinary;

	@Value("${app.execution.docker.timeout-seconds:10}")
	private int timeoutSeconds;

	@Value("${app.execution.docker.memory:256m}")
	private String memory;

	@Value("${app.execution.docker.cpus:0.5}")
	private String cpus;

	@Value("${app.execution.docker.pids-limit:128}")
	private int pidsLimit;

	@Value("${app.execution.docker.max-output-bytes:1048576}")
	private int maxOutputBytes;

	public ExecutionResult run(UUID executionId, String runtimeImage, String code) {
		var containerName = "codecollab-exec-" + executionId;
		var command = buildCommand(containerName, runtimeImage);
		log.debug("Running execution {} with image {}", executionId, runtimeImage);

		var start = System.currentTimeMillis();
		Process process;
		try {
			process = new ProcessBuilder(command).start();
		} catch (IOException ex) {
			throw new IllegalStateException("Failed to start Docker process: " + ex.getMessage(), ex);
		}

		writeCodeToStdin(process, code);

		var stdoutReader = new StreamCollector(process.getInputStream(), maxOutputBytes);
		var stderrReader = new StreamCollector(process.getErrorStream(), maxOutputBytes);
		stdoutReader.start();
		stderrReader.start();

		boolean finished;
		try {
			finished = process.waitFor(timeoutSeconds, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			killContainer(containerName);
			process.destroyForcibly();
			throw new IllegalStateException("Execution was interrupted", ex);
		}

		if (!finished) {
			killContainer(containerName);
			process.destroyForcibly();
			waitQuietly(stdoutReader);
			waitQuietly(stderrReader);
			var durationMs = (int) (System.currentTimeMillis() - start);
			log.info("Execution {} timed out after {}s", executionId, timeoutSeconds);
			return new ExecutionResult(stdoutReader.asString(),
					"Execution timed out after " + timeoutSeconds + " seconds.\n",
					124, durationMs, true);
		}

		waitQuietly(stdoutReader);
		waitQuietly(stderrReader);
		var durationMs = (int) (System.currentTimeMillis() - start);
		var exitCode = process.exitValue();

		if (isDockerInfrastructureFailure(exitCode)) {
			throw new IllegalStateException("Docker failed to run the container (exit " + exitCode + "): "
					+ stderrReader.asString());
		}

		return new ExecutionResult(stdoutReader.asString(), stderrReader.asString(), exitCode, durationMs, false);
	}

	private boolean isDockerInfrastructureFailure(int exitCode) {
		return exitCode == DOCKER_DAEMON_ERROR_EXIT_CODE
				|| exitCode == CONTAINER_CANNOT_INVOKE_EXIT_CODE
				|| exitCode == CONTAINER_COMMAND_NOT_FOUND_EXIT_CODE;
	}

	private List<String> buildCommand(String containerName, String runtimeImage) {
		var command = new ArrayList<String>();
		command.add(dockerBinary);
		command.add("run");
		command.add("--rm");
		command.add("-i");
		command.add("--name");
		command.add(containerName);
		command.add("--network");
		command.add("none");
		command.add("--memory");
		command.add(memory);
		command.add("--memory-swap");
		command.add(memory);
		command.add("--cpus");
		command.add(cpus);
		command.add("--pids-limit");
		command.add(String.valueOf(pidsLimit));
		command.add("--read-only");
		command.add("--user");
		command.add("65534:65534");
		command.add(runtimeImage);
		command.add("python3");
		command.add("-");
		return command;
	}

	private void writeCodeToStdin(Process process, String code) {
		try (OutputStream stdin = process.getOutputStream()) {
			stdin.write(code.getBytes(StandardCharsets.UTF_8));
			stdin.flush();
		} catch (IOException ex) {
			log.warn("Failed to write code to Docker stdin: {}", ex.getMessage());
		}
	}

	private void killContainer(String containerName) {
		try {
			var killProcess = new ProcessBuilder(dockerBinary, "kill", containerName)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.redirectError(ProcessBuilder.Redirect.DISCARD)
					.start();
			killProcess.waitFor(5, TimeUnit.SECONDS);
		} catch (IOException ex) {
			log.warn("Failed to kill container {}: {}", containerName, ex.getMessage());
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private void waitQuietly(Thread thread) {
		try {
			thread.join(TimeUnit.SECONDS.toMillis(5));
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}

	private static final class StreamCollector extends Thread {

		private final InputStream source;
		private final int maxBytes;
		private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

		private StreamCollector(InputStream source, int maxBytes) {
			this.source = source;
			this.maxBytes = maxBytes;
			setDaemon(true);
		}

		@Override
		public void run() {
			var chunk = new byte[8192];
			try {
				int read;
				while ((read = source.read(chunk)) != -1) {
					var remaining = maxBytes - buffer.size();
					if (remaining <= 0) {
						continue;
					}
					buffer.write(chunk, 0, Math.min(read, remaining));
				}
			} catch (IOException ex) {
				log.debug("Stream collection ended: {}", ex.getMessage());
			}
		}

		private String asString() {
			var text = buffer.toString(StandardCharsets.UTF_8);
			return text.isEmpty() ? null : text;
		}
	}
}
