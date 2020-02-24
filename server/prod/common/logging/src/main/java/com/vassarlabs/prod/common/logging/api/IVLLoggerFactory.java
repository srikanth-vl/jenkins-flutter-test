package com.vassarlabs.prod.common.logging.api;

public interface IVLLoggerFactory {

	IVLLogger getLogger(String name);
	
	IVLLogger getLogger(Class<?> clazz);
	
}
