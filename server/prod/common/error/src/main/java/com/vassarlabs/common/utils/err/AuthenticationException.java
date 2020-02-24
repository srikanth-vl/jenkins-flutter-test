package com.vassarlabs.common.utils.err;

public class AuthenticationException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2780259402050950482L;
	public AuthenticationException() {
		super();
	}
	
	public AuthenticationException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public AuthenticationException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public AuthenticationException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public AuthenticationException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public AuthenticationException(IErrorObject errorObject) {
		super(errorObject);
	}

	public AuthenticationException(String errorMessage) {
		super(errorMessage);
	}
	
	public AuthenticationException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
	
}
