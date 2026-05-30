package com.codecollab.user_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.util.Messages;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.RollbackException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
@ApiResponses(value = {
		@ApiResponse(responseCode = "400", description = "Bad Request",
				content = @Content(schema = @Schema(implementation = BaseController.ErrorDetails.class))),
		@ApiResponse(responseCode = "401", description = "Unauthorized"),
		@ApiResponse(responseCode = "403", description = "Forbidden",
				content = @Content(schema = @Schema(implementation = BaseController.ErrorDetails.class))),
		@ApiResponse(responseCode = "404", description = "Not Found",
				content = @Content(schema = @Schema(implementation = BaseController.ErrorDetails.class))),
		@ApiResponse(responseCode = "500", description = "Internal Server Error",
				content = @Content(schema = @Schema(implementation = BaseController.ErrorDetails.class)))
})
public abstract class BaseController {

	public static final String DATA_INTEGRITY_ERROR = "duplicate-key-error";

	@Autowired
	protected Messages messages;

	@ExceptionHandler(AppException.class)
	public ResponseEntity<ErrorDetails> handleAppException(AppException ex) {
		var status = statusFor(ex.getCode());

		if (AppException.INTERNAL_ERROR.equals(ex.getCode()) || AppException.DATABASE_ERROR.equals(ex.getCode())) {
			log.error("Application error [{}]", ex.getCode(), ex);
			return ResponseEntity.status(status)
					.body(new ErrorDetails(ex.getCode(), messages.get(Messages.INTERNAL_ERROR_MSG_KEY)));
		}

		return ResponseEntity.status(status).body(new ErrorDetails(ex.getCode(), ex.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDetails> handleValidation(MethodArgumentNotValidException ex) {
		var message = ex.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(FieldError::getDefaultMessage)
				.orElse(ex.getMessage());
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDetails(AppException.VALIDATION_ERROR, message));
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ErrorDetails> handleConstraintViolation(ConstraintViolationException ex) {
		var message = ex.getConstraintViolations().iterator().next().getMessage();
		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDetails(AppException.VALIDATION_ERROR, message));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorDetails> handleDataIntegrity(DataIntegrityViolationException ex) {
		log.warn("Data integrity violation", ex);
		return ResponseEntity.status(HttpStatus.BAD_REQUEST)
				.body(new ErrorDetails(DATA_INTEGRITY_ERROR, messages.get("dataIntegrityError")));
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorDetails> handleAccessDenied(AccessDeniedException ex) {
		return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorDetails(AppException.FORBIDDEN_ERROR, ex.getMessage()));
	}

	@ExceptionHandler(TransactionSystemException.class)
	public ResponseEntity<ErrorDetails> handleTransaction(TransactionSystemException ex) {
		var cause = ex.getCause();
		if (cause instanceof RollbackException && cause.getCause() instanceof ConstraintViolationException cve) {
			var message = cve.getConstraintViolations().iterator().next().getMessage();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorDetails(AppException.VALIDATION_ERROR, message));
		}
		log.error("Transaction system error", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorDetails(AppException.INTERNAL_ERROR, messages.get(Messages.INTERNAL_ERROR_MSG_KEY)));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorDetails> handleException(Exception ex) {
		log.error("Unhandled exception", ex);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
				.body(new ErrorDetails(AppException.INTERNAL_ERROR, messages.get(Messages.INTERNAL_ERROR_MSG_KEY)));
	}

	private HttpStatus statusFor(String code) {
		return switch (code) {
			case AppException.NOT_FOUND_ERROR -> HttpStatus.NOT_FOUND;
			case AppException.FORBIDDEN_ERROR -> HttpStatus.FORBIDDEN;
			case AppException.VALIDATION_ERROR -> HttpStatus.BAD_REQUEST;
			case AppException.INTERNAL_ERROR, AppException.DATABASE_ERROR, AppException.INVALID_CLASS_NAME ->
					HttpStatus.INTERNAL_SERVER_ERROR;
			default -> HttpStatus.BAD_REQUEST;
		};
	}

	public record ErrorDetails(String code, String message) {}
}
