package com.vassarlabs.common.utils.err;

public class SMSSendException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3524625597548444373L;

	public SMSSendException() {
		super();
	}

	public SMSSendException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public SMSSendException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public SMSSendException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public SMSSendException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public SMSSendException(IErrorObject errorObject) {
		super(errorObject);
	}

	public SMSSendException(String errorMessage) {
		super(errorMessage);
	}

	public SMSSendException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}