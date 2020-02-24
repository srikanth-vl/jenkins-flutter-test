package com.vassarlabs.common.utils.err;

public class ValidationException
	extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3889914400461206582L;

	public ValidationException() {
		super();
	}

	public ValidationException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public ValidationException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public ValidationException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public ValidationException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public ValidationException(IErrorObject errorObject) {
		super(errorObject);
	}

	public ValidationException(String errorMessage) {
		super(errorMessage);
	}
	
	public ValidationException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
