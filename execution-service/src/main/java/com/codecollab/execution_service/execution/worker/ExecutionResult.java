package com.codecollab.execution_service.execution.worker;

public record ExecutionResult(String stdout, String stderr, int exitCode, int durationMs, boolean timedOut) {
}
