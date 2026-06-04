package com.codecollab.api_gateway.auth;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionUserDto {

	private UUID id;
	private String username;
}
