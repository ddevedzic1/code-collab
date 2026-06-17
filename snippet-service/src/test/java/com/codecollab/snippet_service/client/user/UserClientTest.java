package com.codecollab.snippet_service.client.user;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
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
		classes = UserClientTest.TestApp.class,
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureWireMock(port = 0)
@TestPropertySource(properties = {
		"spring.cloud.discovery.enabled=false",
		"eureka.client.enabled=false",
		"spring.cloud.openfeign.client.config.user-service.url=http://localhost:${wiremock.server.port}"
})
class UserClientTest {

	@Configuration
	@EnableAutoConfiguration
	@EnableFeignClients(clients = UserClient.class)
	static class TestApp {
	}

	@Autowired
	private UserClient userClient;

	@Test
	void lookupByUsername_callsCorrectPath_andDeserializesDto() {
		var id = UUID.randomUUID();
		stubFor(get(urlPathEqualTo("/api/v1/users/by-username/alice"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("{\"id\":\"" + id + "\",\"username\":\"alice\"}")));

		var result = userClient.lookupByUsername("alice");

		assertThat(result.getId()).isEqualTo(id);
		assertThat(result.getUsername()).isEqualTo("alice");
	}

	@Test
	void lookupByUsername_toleratesUnknownFields() {
		var id = UUID.randomUUID();
		stubFor(get(urlPathEqualTo("/api/v1/users/by-username/bob"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("{\"id\":\"" + id + "\",\"username\":\"bob\",\"email\":\"bob@x.io\",\"extra\":42}")));

		var result = userClient.lookupByUsername("bob");

		assertThat(result.getUsername()).isEqualTo("bob");
	}

	@Test
	void lookupByUsername_propagatesNotFound() {
		stubFor(get(urlPathEqualTo("/api/v1/users/by-username/ghost"))
				.willReturn(aResponse().withStatus(404)));

		assertThatThrownBy(() -> userClient.lookupByUsername("ghost"))
				.isInstanceOf(FeignException.NotFound.class);
	}

	@Test
	void lookupByUsername_propagatesServerError() {
		stubFor(get(urlPathEqualTo("/api/v1/users/by-username/boom"))
				.willReturn(aResponse().withStatus(500)));

		assertThatThrownBy(() -> userClient.lookupByUsername("boom"))
				.isInstanceOf(FeignException.class);
	}

	@Test
	void lookupByIds_sendsIdsAsQueryParam_andDeserializesList() {
		var id1 = UUID.randomUUID();
		var id2 = UUID.randomUUID();
		stubFor(get(urlPathMatching("/api/v1/users/lookup"))
				.willReturn(aResponse()
						.withHeader("Content-Type", "application/json")
						.withBody("[{\"id\":\"" + id1 + "\",\"username\":\"a\"},"
								+ "{\"id\":\"" + id2 + "\",\"username\":\"b\"}]")));

		List<UserLookupClientDto> result = userClient.lookupByIds(List.of(id1, id2));

		assertThat(result).extracting(UserLookupClientDto::getUsername).containsExactly("a", "b");
	}
}
