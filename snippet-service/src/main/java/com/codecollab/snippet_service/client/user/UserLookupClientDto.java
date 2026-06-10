package com.codecollab.snippet_service.client.user;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserLookupClientDto {

	private UUID id;
	private String username;
}
