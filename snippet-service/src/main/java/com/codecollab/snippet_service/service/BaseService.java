package com.codecollab.snippet_service.service;

import org.modelmapper.ModelMapper;

import com.codecollab.snippet_service.util.Messages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseService {

	protected final Messages messages;
	protected final ModelMapper modelMapper;
}
