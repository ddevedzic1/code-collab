package com.codecollab.user_service.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import com.codecollab.user_service.util.Messages;

public abstract class BaseService {

	@Autowired
	protected Messages messages;

	@Autowired
	protected ModelMapper modelMapper;
}
