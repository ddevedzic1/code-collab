package com.codecollab.execution_service.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;

import com.codecollab.execution_service.util.Messages;

/**
 * Shared @WebMvcTest support: supplies the {@link Messages} bean (and its backing
 * {@link MessageSource}) that controllers inherit from BaseController, since
 * @WebMvcTest does not scan @Component classes.
 */
@TestConfiguration
public class WebTestSupport {

	@Bean
	public MessageSource messageSource() {
		var source = new ResourceBundleMessageSource();
		source.setBasename("messages");
		source.setDefaultEncoding("UTF-8");
		return source;
	}

	@Bean
	public Messages messages(MessageSource messageSource) {
		return new Messages(messageSource);
	}
}
