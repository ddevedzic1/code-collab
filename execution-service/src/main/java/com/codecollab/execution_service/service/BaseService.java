package com.codecollab.execution_service.service;

import org.modelmapper.ModelMapper;

import com.codecollab.execution_service.util.Messages;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class BaseService {

	protected final Messages messages;
	protected final ModelMapper modelMapper;
}
