package com.codecollab.snippet_service.client.user;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserClient {

	@GetMapping("/api/v1/users/by-username/{username}")
	UserLookupClientDto lookupByUsername(@PathVariable("username") String username);
}
