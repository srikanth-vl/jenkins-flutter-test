package com.vassarlabs.prod.common.logging.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.common.logging.api.IVLLoggerFactory;

@Component
public class VLLogService implements IVLLogService {
	
	@Autowired
	protected IVLLoggerFactory factory;
	
	@Override
	public IVLLogger getLogger(String name){
		return factory.getLogger(name);
	}
	
	@Override
	public IVLLogger getLogger(Class<?> clazz){
		return factory.getLogger(clazz);
	}
}
