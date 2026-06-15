package com.codecollab.execution_service.execution.worker;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class DockerExecutionRunnerTest {

	private DockerExecutionRunner runner;

	@BeforeEach
	void setUp() {
		runner = new DockerExecutionRunner();
		ReflectionTestUtils.setField(runner, "dockerBinary", "docker");
		ReflectionTestUtils.setField(runner, "timeoutSeconds", 10);
		ReflectionTestUtils.setField(runner, "memory", "256m");
		ReflectionTestUtils.setField(runner, "cpus", "0.5");
		ReflectionTestUtils.setField(runner, "pidsLimit", 128);
		ReflectionTestUtils.setField(runner, "maxOutputBytes", 1048576);
	}

	@Test
	void infrastructureFailure_detectsDockerExitCodes() {
		assertThat(invokeInfraFailure(125)).isTrue();
		assertThat(invokeInfraFailure(126)).isTrue();
		assertThat(invokeInfraFailure(127)).isTrue();
	}

	@Test
	void infrastructureFailure_isFalse_forProgramExitCodes() {
		assertThat(invokeInfraFailure(0)).isFalse();
		assertThat(invokeInfraFailure(1)).isFalse();
		assertThat(invokeInfraFailure(124)).isFalse();
		assertThat(invokeInfraFailure(137)).isFalse();
	}

	@Test
	@SuppressWarnings("unchecked")
	void buildCommand_appliesSandboxFlags() {
		var command = (List<String>) ReflectionTestUtils.invokeMethod(
				runner, "buildCommand", "codecollab-exec-123", "python:3.11-slim");

		assertThat(command).containsSubsequence("docker", "run", "--rm", "-i");
		assertThat(command).containsSubsequence("--network", "none");
		assertThat(command).containsSubsequence("--memory", "256m");
		assertThat(command).containsSubsequence("--memory-swap", "256m");
		assertThat(command).containsSubsequence("--cpus", "0.5");
		assertThat(command).containsSubsequence("--pids-limit", "128");
		assertThat(command).contains("--read-only");
		assertThat(command).containsSubsequence("--user", "65534:65534");
		assertThat(command).containsSubsequence("--name", "codecollab-exec-123");
		assertThat(command).containsSubsequence("python:3.11-slim", "python3", "-");
	}

	@Test
	@SuppressWarnings("unchecked")
	void buildCommand_runsImageAsTheLastImageArgument() {
		var command = (List<String>) ReflectionTestUtils.invokeMethod(
				runner, "buildCommand", "container", "node:20-slim");

		var imageIndex = command.indexOf("node:20-slim");
		assertThat(command.get(imageIndex + 1)).isEqualTo("python3");
		assertThat(command.get(imageIndex + 2)).isEqualTo("-");
	}

	private boolean invokeInfraFailure(int exitCode) {
		return Boolean.TRUE.equals(
				ReflectionTestUtils.invokeMethod(runner, "isDockerInfrastructureFailure", exitCode));
	}
}
