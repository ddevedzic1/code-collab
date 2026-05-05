package com.codecollab.snippet_service.util;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResult<T>(
		List<T> result,
		long totalElements,
		Integer currentPage,
		Integer pageSize,
		Integer totalPages) {

	public static <T> PageResult<T> from(Page<T> page) {
		return new PageResult<>(
				page.getContent(),
				page.getTotalElements(),
				page.getNumber(),
				page.getSize(),
				page.getTotalPages());
	}
}
