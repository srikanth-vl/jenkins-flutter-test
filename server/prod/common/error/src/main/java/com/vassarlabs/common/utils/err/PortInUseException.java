package com.vassarlabs.common.utils.err;

public class PortInUseException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2002461944400877273L;

	public PortInUseException() {
		super();
	}

	public PortInUseException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public PortInUseException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public PortInUseException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public PortInUseException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public PortInUseException(IErrorObject errorObject) {
		super(errorObject);
	}

	public PortInUseException(String errorMessage) {
		super(errorMessage);
	}
	
	public PortInUseException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
