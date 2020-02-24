package com.vassarlabs.common.utils.err;

public class DataNotFoundException extends VLException {

	private static final long serialVersionUID = -2208064753925747958L;

	public DataNotFoundException() {
		super();
	}

	public DataNotFoundException(IErrorObject errorObject, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DataNotFoundException(IErrorObject errorObject, String message, Throwable cause) {
		super(errorObject, message, cause);
	}

	public DataNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DataNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DataNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DataNotFoundException(String errorMessage) {
		super(errorMessage);
	}
	
	public DataNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
