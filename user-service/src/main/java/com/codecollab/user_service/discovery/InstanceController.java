package com.codecollab.user_service.discovery;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/instance")
public class InstanceController {

	private final String instanceId = UUID.randomUUID().toString();

	@Value("${server.port}")
	private int port;

	@GetMapping
	public ResponseEntity<Map<String, Object>> whoAmI() {
		return ResponseEntity.ok(Map.of(
				"instanceId", instanceId,
				"port", port,
				"hostname", resolveHostname()));
	}

	private String resolveHostname() {
		try {
			return java.net.InetAddress.getLocalHost().getHostName();
		} catch (Exception ex) {
			return "unknown";
		}
	}
}
