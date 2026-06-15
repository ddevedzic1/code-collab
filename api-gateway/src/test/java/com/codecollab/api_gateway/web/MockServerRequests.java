package com.codecollab.api_gateway.web;

import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

/**
 * Test helpers that build {@link MockServerHttpRequest} instances for the
 * authentication filter tests.
 */
public final class MockServerRequests {

	private MockServerRequests() {
	}

	private static final String PROTECTED_PATH = "/api/v1/executions";
	private static final String PUBLIC_PATH = "/api/v1/auth/login";

	public static MockServerHttpRequest protectedGet(String cookieHeader) {
		var builder = MockServerHttpRequest.get(PROTECTED_PATH);
		if (cookieHeader != null) {
			builder.header(HttpHeaders.COOKIE, cookieHeader);
		}
		return builder.build();
	}

	public static MockServerHttpRequest protectedGetWithSpoofedUserId(String userId) {
		return MockServerHttpRequest.get(PROTECTED_PATH)
				.header("X-User-Id", userId)
				.build();
	}

	public static MockServerHttpRequest publicGet(String cookieHeader) {
		var builder = MockServerHttpRequest.post(PUBLIC_PATH);
		if (cookieHeader != null) {
			builder.header(HttpHeaders.COOKIE, cookieHeader);
		}
		return builder.build();
	}

	public static MockServerHttpRequest optionsRequest() {
		return MockServerHttpRequest.options(PROTECTED_PATH).build();
	}
}
