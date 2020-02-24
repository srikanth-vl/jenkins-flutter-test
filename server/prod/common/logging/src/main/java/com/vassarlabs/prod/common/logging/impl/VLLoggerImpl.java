package com.vassarlabs.prod.common.logging.impl;

import org.slf4j.Logger;

import com.vassarlabs.prod.common.logging.api.IVLLogger;

public class VLLoggerImpl implements IVLLogger {
	
	private final Logger wrapped;
	
	public VLLoggerImpl(final Logger logger){
		this.wrapped = logger;
	}
	
	@Override
	public void debug(String msg){
		wrapped.debug(msg);
	}
	
	@Override
	public void info(String msg){
		 wrapped.info(msg);
	}

	@Override
	public void warn(String msg){
		 wrapped.warn(msg);
	}
	

	@Override
	public void warn(String msg, Throwable t){
		 wrapped.warn(msg);
	}
	
	@Override
	public void error(String msg){
		 wrapped.error(msg);
	}

	@Override
	public void error(String msg, Throwable t) {
		 wrapped.error(msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return this.wrapped.isDebugEnabled();
	}

}
