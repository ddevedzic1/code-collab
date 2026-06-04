package com.codecollab.api_gateway.auth;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.auth")
public class AuthProperties {

	private List<String> publicPaths = new ArrayList<>();
}
