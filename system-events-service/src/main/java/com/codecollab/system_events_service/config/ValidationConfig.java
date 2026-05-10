package com.codecollab.system_events_service.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class ValidationConfig {

	private final MessageSource messageSource;

	@Bean
	public LocalValidatorFactoryBean validator() {
		var validator = new LocalValidatorFactoryBean();
		validator.setValidationMessageSource(messageSource);
		return validator;
	}
}
