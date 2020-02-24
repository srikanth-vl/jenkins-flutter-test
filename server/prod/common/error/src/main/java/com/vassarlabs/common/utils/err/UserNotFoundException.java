package com.vassarlabs.common.utils.err;

public class UserNotFoundException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2917507940072980200L;
	public UserNotFoundException() {
		super();
	}

	public UserNotFoundException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public UserNotFoundException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public UserNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public UserNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public UserNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public UserNotFoundException(String errorMessage) {
		super(errorMessage);
	}
	
	public UserNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
