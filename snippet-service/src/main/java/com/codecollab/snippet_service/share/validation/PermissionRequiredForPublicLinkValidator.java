package com.codecollab.snippet_service.share.validation;

import com.codecollab.snippet_service.share.dto.ShareCreateDto;
import com.codecollab.snippet_service.share.model.ShareType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PermissionRequiredForPublicLinkValidator
		implements ConstraintValidator<PermissionRequiredForPublicLink, ShareCreateDto> {

	@Override
	public boolean isValid(ShareCreateDto dto, ConstraintValidatorContext context) {
		if (dto == null || dto.getShareType() != ShareType.PUBLIC_LINK) {
			return true;
		}
		if (dto.getPermission() != null) {
			return true;
		}
		context.disableDefaultConstraintViolation();
		context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
				.addPropertyNode("permission")
				.addConstraintViolation();
		return false;
	}
}
