package com.vassarlabs.common.utils.err;

public class MobileNoNotFoundException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4696026439695082576L;

	public MobileNoNotFoundException() {
		super();
	}

	public MobileNoNotFoundException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public MobileNoNotFoundException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public MobileNoNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public MobileNoNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public MobileNoNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public MobileNoNotFoundException(String errorMessage) {
		super(errorMessage);
	}

	public MobileNoNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}


