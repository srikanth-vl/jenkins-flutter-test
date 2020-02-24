package com.vassarlabs.common.utils.err;

public class InvalidInputParamException extends VLException {

	private static final long serialVersionUID = -4189793594233999823L;

	public InvalidInputParamException() {
		super();
	}

	public InvalidInputParamException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidInputParamException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public InvalidInputParamException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public InvalidInputParamException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public InvalidInputParamException(IErrorObject errorObject) {
		super(errorObject);
	}

	public InvalidInputParamException(String errorMessage) {
		super(errorMessage);
	}
	
	public InvalidInputParamException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
