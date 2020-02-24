package com.vassarlabs.common.utils.err;

public class OldAndNewPasswordMatchException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5697557535798464284L;

	public OldAndNewPasswordMatchException() {
		super();
	}

	public OldAndNewPasswordMatchException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public OldAndNewPasswordMatchException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public OldAndNewPasswordMatchException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public OldAndNewPasswordMatchException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public OldAndNewPasswordMatchException(IErrorObject errorObject) {
		super(errorObject);
	}

	public OldAndNewPasswordMatchException(String errorMessage) {
		super(errorMessage);
	}

	public OldAndNewPasswordMatchException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
