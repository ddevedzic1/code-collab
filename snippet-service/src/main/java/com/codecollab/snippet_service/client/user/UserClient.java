package com.codecollab.snippet_service.client.user;

import java.util.List;
import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserClient {

	@GetMapping("/api/v1/users/by-username/{username}")
	UserLookupClientDto lookupByUsername(@PathVariable("username") String username);

	@GetMapping("/api/v1/users/lookup")
	List<UserLookupClientDto> lookupByIds(@RequestParam("ids") List<UUID> ids);
}
