package com.vassarlabs.common.utils.err;

public class InvalidPasswordException extends VLException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2889879144600537152L;

	public InvalidPasswordException() {
		super();
	}

	public InvalidPasswordException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidPasswordException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public InvalidPasswordException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public InvalidPasswordException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public InvalidPasswordException(IErrorObject errorObject) {
		super(errorObject);
	}

	public InvalidPasswordException(String errorMessage) {
		super(errorMessage);
	}

	public InvalidPasswordException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
