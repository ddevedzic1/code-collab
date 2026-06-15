package com.codecollab.snippet_service.share.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codecollab.snippet_service.share.dto.ShareCreateDto;
import com.codecollab.snippet_service.share.model.Permission;
import com.codecollab.snippet_service.share.model.ShareType;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import jakarta.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;

class PermissionRequiredForPublicLinkValidatorTest {

	private PermissionRequiredForPublicLinkValidator validator;
	private ConstraintValidatorContext context;

	@BeforeEach
	void setUp() {
		validator = new PermissionRequiredForPublicLinkValidator();
		context = mock(ConstraintValidatorContext.class);
		var builder = mock(ConstraintViolationBuilder.class);
		var node = mock(NodeBuilderCustomizableContext.class);
		lenient().when(context.getDefaultConstraintMessageTemplate()).thenReturn("message");
		lenient().when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
		lenient().when(builder.addPropertyNode(anyString())).thenReturn(node);
		lenient().when(node.addConstraintViolation()).thenReturn(context);
	}

	@Test
	void valid_whenNullDto() {
		assertThat(validator.isValid(null, context)).isTrue();
	}

	@Test
	void valid_whenPublicLinkHasPermission() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.PUBLIC_LINK);
		dto.setPermission(Permission.READ_ONLY);

		assertThat(validator.isValid(dto, context)).isTrue();
	}

	@Test
	void invalid_whenPublicLinkMissingPermission() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.PUBLIC_LINK);
		dto.setPermission(null);

		assertThat(validator.isValid(dto, context)).isFalse();
	}

	@Test
	void valid_whenUserShareMissingPermission() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.USER);
		dto.setPermission(null);

		assertThat(validator.isValid(dto, context)).isTrue();
	}

	@Test
	void valid_whenUserShareHasPermission() {
		var dto = new ShareCreateDto();
		dto.setShareType(ShareType.USER);
		dto.setPermission(Permission.EDIT);

		assertThat(validator.isValid(dto, context)).isTrue();
	}

	@Test
	void valid_whenShareTypeNull() {
		var dto = new ShareCreateDto();
		dto.setShareType(null);
		dto.setPermission(null);

		assertThat(validator.isValid(dto, context)).isTrue();
	}
}
