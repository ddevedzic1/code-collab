package com.codecollab.api_gateway.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.codecollab.api_gateway.web.MockServerRequests;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AuthenticationGlobalFilterTest {

	private static final String USER_ID_HEADER = "X-User-Id";

	private AuthProperties authProperties() {
		var props = new AuthProperties();
		props.setPublicPaths(List.of(
				"/api/v1/auth/login",
				"/api/v1/auth/register",
				"/api/v1/shares/by-token/**"));
		return props;
	}

	/**
	 * Builds a filter whose WebClient is backed by a canned exchange function,
	 * avoiding any real network / load-balancer call.
	 */
	private AuthenticationGlobalFilter filterWith(ExchangeFunction exchangeFunction) {
		var builder = WebClient.builder().exchangeFunction(exchangeFunction);
		return new AuthenticationGlobalFilter(builder, authProperties());
	}

	private ExchangeFunction sessionResponse(HttpStatus status, String json) {
		return request -> {
			var response = ClientResponse.create(status)
					.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
			if (json != null) {
				response = response.body(json);
			}
			return Mono.just(response.build());
		};
	}

	private ExchangeFunction failingResponse() {
		return request -> Mono.error(new RuntimeException("user-service unreachable"));
	}

	@Test
	void protectedRoute_withoutCookie_returns401() {
		var filter = filterWith(sessionResponse(HttpStatus.OK, null));
		var exchange = MockServerWebExchange.from(MockServerRequests.protectedGet(null));
		var chain = mock(GatewayFilterChain.class);

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(chain, never()).filter(any());
	}

	@Test
	void publicRoute_withoutCookie_passesThroughAnonymously() {
		var filter = filterWith(sessionResponse(HttpStatus.OK, null));
		var exchange = MockServerWebExchange.from(MockServerRequests.publicGet(null));
		var chain = passThroughChain();

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		verify(chain).filter(any());
	}

	@Test
	void protectedRoute_withValidSession_injectsUserIdAndRoutes() {
		var userId = UUID.randomUUID();
		var filter = filterWith(
				sessionResponse(HttpStatus.OK, "{\"id\":\"" + userId + "\",\"username\":\"alice\"}"));
		var exchange = MockServerWebExchange.from(MockServerRequests.protectedGet("SESSION=abc"));
		var capturingChain = new CapturingChain();

		StepVerifier.create(filter.filter(exchange, capturingChain)).verifyComplete();

		assertThat(capturingChain.captured).isNotNull();
		assertThat(capturingChain.captured.getRequest().getHeaders().getFirst(USER_ID_HEADER))
				.isEqualTo(userId.toString());
	}

	@Test
	void protectedRoute_withInvalidSession_returns401() {
		var filter = filterWith(sessionResponse(HttpStatus.OK, "{\"id\":null}"));
		var exchange = MockServerWebExchange.from(MockServerRequests.protectedGet("SESSION=bad"));
		var chain = mock(GatewayFilterChain.class);

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
		verify(chain, never()).filter(any());
	}

	@Test
	void downstreamFailure_propagatesError_doesNotMaskAs401() {
		var userId = UUID.randomUUID();
		var filter = filterWith(
				sessionResponse(HttpStatus.OK, "{\"id\":\"" + userId + "\",\"username\":\"alice\"}"));
		var exchange = MockServerWebExchange.from(MockServerRequests.protectedGet("SESSION=abc"));
		GatewayFilterChain failingChain = ex ->
				Mono.error(new RuntimeException("execution-service unavailable"));

		StepVerifier.create(filter.filter(exchange, failingChain))
				.expectErrorMessage("execution-service unavailable")
				.verify();

		assertThat(exchange.getResponse().getStatusCode()).isNull();
	}

	@Test
	void optionsRequest_bypassesAuthentication() {
		var filter = filterWith(failingResponse());
		var exchange = MockServerWebExchange.from(MockServerRequests.optionsRequest());
		var chain = passThroughChain();

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		verify(chain).filter(any());
	}

	@Test
	void existingUserIdHeader_isStripped_beforeAuthentication() {
		var filter = filterWith(sessionResponse(HttpStatus.OK, null));
		var exchange = MockServerWebExchange.from(
				MockServerRequests.protectedGetWithSpoofedUserId("11111111-1111-1111-1111-111111111111"));
		var chain = mock(GatewayFilterChain.class);

		StepVerifier.create(filter.filter(exchange, chain)).verifyComplete();

		assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	private GatewayFilterChain passThroughChain() {
		var chain = mock(GatewayFilterChain.class);
		when(chain.filter(any())).thenReturn(Mono.empty());
		return chain;
	}

	private static final class CapturingChain implements GatewayFilterChain {
		private ServerWebExchange captured;

		@Override
		public Mono<Void> filter(ServerWebExchange exchange) {
			this.captured = exchange;
			return Mono.empty();
		}
	}
}
