package com.codecollab.api_gateway.auth;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class AuthenticationGlobalFilter implements GlobalFilter, Ordered {

	public static final String USER_ID_HEADER = "X-User-Id";

	private static final String VALIDATE_URI = "lb://user-service/api/v1/auth/validate";

	private final AntPathMatcher pathMatcher = new AntPathMatcher();
	private final WebClient webClient;
	private final List<String> publicPaths;

	public AuthenticationGlobalFilter(WebClient.Builder loadBalancedWebClientBuilder,
			AuthProperties authProperties) {
		this.webClient = loadBalancedWebClientBuilder.build();
		this.publicPaths = authProperties.getPublicPaths();
	}

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		var request = exchange.getRequest();
		var path = request.getURI().getPath();
		var method = request.getMethod();

		var sanitizedExchange = exchange.mutate()
				.request(r -> r.headers(headers -> headers.remove(USER_ID_HEADER)))
				.build();

		if (method != null && "OPTIONS".equalsIgnoreCase(method.name())) {
			return chain.filter(sanitizedExchange);
		}

		if (isPublic(path)) {
			return chain.filter(sanitizedExchange);
		}

		var cookieHeader = request.getHeaders().getFirst(HttpHeaders.COOKIE);
		if (cookieHeader == null || cookieHeader.isBlank()) {
			return unauthorized(exchange);
		}

		return webClient.get()
				.uri(VALIDATE_URI)
				.header(HttpHeaders.COOKIE, cookieHeader)
				.retrieve()
				.bodyToMono(SessionUserDto.class)
				.flatMap(sessionUser -> {
					if (sessionUser == null || sessionUser.getId() == null) {
						return unauthorized(exchange);
					}
					var authenticatedExchange = sanitizedExchange.mutate()
							.request(r -> r.header(USER_ID_HEADER, sessionUser.getId().toString()))
							.build();
					return chain.filter(authenticatedExchange);
				})
				.onErrorResume(ex -> {
					log.warn("Session validation failed for path {}: {}", path, ex.getMessage());
					return unauthorized(exchange);
				});
	}

	private boolean isPublic(String path) {
		return publicPaths.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
	}

	private Mono<Void> unauthorized(ServerWebExchange exchange) {
		exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
		return exchange.getResponse().setComplete();
	}

	@Override
	public int getOrder() {
		return -1;
	}
}
