package com.codecollab.snippet_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfig {

	@Bean
	public OpenAPI openApi() {
		return new OpenAPI().info(new Info()
				.title("Snippet Service")
				.description("REST API for the Code Collab snippet service.")
				.version("v1.0.0"));
	}
}
