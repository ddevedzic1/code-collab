package com.codecollab.user_service.auth.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codecollab.user_service.auth.dto.LoginRequestDto;
import com.codecollab.user_service.auth.dto.RegisterRequestDto;
import com.codecollab.user_service.auth.dto.SessionUserDto;
import com.codecollab.user_service.auth.service.AuthService;
import com.codecollab.user_service.controller.BaseController;
import com.codecollab.user_service.exception.AppException;
import com.codecollab.user_service.user.dto.UserResponseDto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

	private final AuthService authService;

	private final SecurityContextRepository securityContextRepository =
			new HttpSessionSecurityContextRepository();

	@PostMapping("/register")
	public ResponseEntity<UserResponseDto> register(@Valid @RequestBody RegisterRequestDto dto) {
		var response = authService.register(dto);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	@PostMapping("/login")
	public ResponseEntity<SessionUserDto> login(@Valid @RequestBody LoginRequestDto dto,
			HttpServletRequest request, HttpServletResponse response) {
		var sessionUser = authService.authenticate(dto);

		var authentication = new UsernamePasswordAuthenticationToken(
				sessionUser.getId().toString(),
				null,
				List.of(new SimpleGrantedAuthority("ROLE_USER")));

		var context = SecurityContextHolder.createEmptyContext();
		context.setAuthentication(authentication);
		SecurityContextHolder.setContext(context);
		securityContextRepository.saveContext(context, request, response);

		return ResponseEntity.ok(sessionUser);
	}

	@GetMapping("/validate")
	public ResponseEntity<SessionUserDto> validate(HttpServletRequest request) {
		var session = request.getSession(false);
		var context = session == null ? null
				: (SecurityContext) session.getAttribute(
						HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);

		if (context == null || context.getAuthentication() == null
				|| !context.getAuthentication().isAuthenticated()) {
			throw new AppException(AppException.UNAUTHORIZED_ERROR,
					messages.get("error.auth.session.invalid"));
		}

		var userId = UUID.fromString(context.getAuthentication().getName());
		return ResponseEntity.ok(authService.getActiveUser(userId));
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(HttpServletRequest request) {
		HttpSession session = request.getSession(false);
		if (session != null) {
			session.invalidate();
		}
		SecurityContextHolder.clearContext();
		return ResponseEntity.noContent().build();
	}
}
