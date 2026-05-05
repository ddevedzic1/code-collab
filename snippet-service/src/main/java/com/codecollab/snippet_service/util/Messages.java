package com.codecollab.snippet_service.util;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.stereotype.Component;

@Component
public class Messages {

	public static final String INTERNAL_ERROR_MSG_KEY = "internalError";

	private final MessageSourceAccessor accessor;

	public Messages(MessageSource messageSource) {
		this.accessor = new MessageSourceAccessor(messageSource, LocaleContextHolder.getLocale());
	}

	public String get(String code, Object... args) {
		return accessor.getMessage(code, args);
	}

	public String getOrDefault(String code, String defaultMessage) {
		return accessor.getMessage(code, defaultMessage);
	}
}
