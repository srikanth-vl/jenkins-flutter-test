package com.vassarlabs.common.utils.err;

public class InvalidInputException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3541486901702685705L;
	
	public InvalidInputException() {
		super();
	}

	public InvalidInputException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidInputException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public InvalidInputException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public InvalidInputException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public InvalidInputException(IErrorObject errorObject) {
		super(errorObject);
	}

	public InvalidInputException(String errorMessage) {
		super(errorMessage);
	}
	
	public InvalidInputException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
