package com.codecollab.execution_service.client.snippet;

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "snippet-service")
public interface SnippetClient {

	@GetMapping("/api/v1/snippets/{id}")
	SnippetClientDto getById(@PathVariable("id") UUID id, @RequestHeader("X-User-Id") UUID callerUserId);
}
