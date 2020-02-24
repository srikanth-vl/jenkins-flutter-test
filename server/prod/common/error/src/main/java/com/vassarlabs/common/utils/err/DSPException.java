package com.vassarlabs.common.utils.err;

public class DSPException
	extends VLException {

	private static final long serialVersionUID = -4189793594233999823L;

	public DSPException() {
		super();
	}

	public DSPException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DSPException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public DSPException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DSPException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DSPException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DSPException(String errorMessage) {
		super(errorMessage);
	}
	
	public DSPException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}