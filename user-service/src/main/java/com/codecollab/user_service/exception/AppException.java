package com.codecollab.user_service.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public static final String INTERNAL_ERROR = "internal-error";
	public static final String FORBIDDEN_ERROR = "forbidden";
	public static final String DATABASE_ERROR = "db-error";
	public static final String INVALID_CLASS_NAME = "invalid-classname";
	public static final String VALIDATION_ERROR = "validation-error";
	public static final String NOT_FOUND_ERROR = "not-found";
	public static final String UNAUTHORIZED_ERROR = "unauthorized";
	public static final String SERVICE_UNAVAILABLE_ERROR = "service-unavailable";

	private final String code;

	public AppException(String code, String message) {
		super(message);
		this.code = code;
	}

	public AppException(String code, String message, Throwable cause) {
		super(message, cause);
		this.code = code;
	}
}
