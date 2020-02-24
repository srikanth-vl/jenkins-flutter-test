package com.vassarlabs.prod.common.logging.api;

public interface IVLLogger {

	public void debug(String msg);
	
	public void info(String msg);
	
	public void warn(String msg);
	
	public void warn(String msg, Throwable t);
	
	public void error(String msg);
	
	public void error(String msg, Throwable t);

	public boolean isDebugEnabled();
}
