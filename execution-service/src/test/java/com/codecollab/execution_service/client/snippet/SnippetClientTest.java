package com.codecollab.execution_service.client.snippet;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;

import feign.FeignException;

@SpringBootTest(
		classes = SnippetClientTest.TestApp.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.cloud.openfeign.circuitbreaker.enabled=false",
		"spring.cloud.openfeign.client.config.snippet-service.url=http://localhost:${wiremock.server.port}"
})
class SnippetClientTest {

	@Configuration
	@EnableAutoConfiguration
	@EnableFeignClients(clients = SnippetClient.class)
	static class TestApp {
	}

	@Autowired
	private SnippetClient snippetClient;

	private final UUID snippetId = UUID.randomUUID();
	private final UUID callerId = UUID.randomUUID();

	@Test
	void getById_callsCorrectPath_andDeserializesSnippetWithLanguage() {
		var userId = UUID.randomUUID();
		var languageId = UUID.randomUUID();
		stubFor(get(urlPathMatching("/api/v1/snippets/.*"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("{"
								+ "\"id\":\"" + snippetId + "\","
								+ "\"userId\":\"" + userId + "\","
								+ "\"content\":\"print('hi')\","
								+ "\"language\":{\"id\":\"" + languageId + "\",\"runtimeImage\":\"python:3.11-slim\"}"
								+ "}")));

		var result = snippetClient.getById(snippetId, callerId);

		assertThat(result.getId()).isEqualTo(snippetId);
		assertThat(result.getContent()).isEqualTo("print('hi')");
		assertThat(result.getLanguage().getRuntimeImage()).isEqualTo("python:3.11-slim");
	}

	@Test
	void getById_toleratesUnknownFields() {
		stubFor(get(urlPathMatching("/api/v1/snippets/.*"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("{"
								+ "\"id\":\"" + snippetId + "\","
								+ "\"title\":\"extra field server added\","
								+ "\"language\":{\"id\":\"" + UUID.randomUUID() + "\",\"runtimeImage\":\"x\",\"name\":\"Y\"}"
								+ "}")));

		var result = snippetClient.getById(snippetId, callerId);

		assertThat(result.getId()).isEqualTo(snippetId);
	}

	@Test
	void getById_propagatesNotFound() {
		stubFor(get(urlPathMatching("/api/v1/snippets/.*"))
				.willReturn(aResponse().withStatus(404)));

		assertThatThrownBy(() -> snippetClient.getById(snippetId, callerId))
				.isInstanceOf(FeignException.NotFound.class);
	}

	@Test
	void getById_propagatesForbidden() {
		stubFor(get(urlPathMatching("/api/v1/snippets/.*"))
				.willReturn(aResponse().withStatus(403)));

		assertThatThrownBy(() -> snippetClient.getById(snippetId, callerId))
				.isInstanceOf(FeignException.Forbidden.class);
	}

	@Test
	void getById_propagatesServerError() {
		stubFor(get(urlPathMatching("/api/v1/snippets/.*"))
				.willReturn(aResponse().withStatus(500)));

		assertThatThrownBy(() -> snippetClient.getById(snippetId, callerId))
				.isInstanceOf(FeignException.class);
	}
}
