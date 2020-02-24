package com.vassarlabs.common.utils.err;

public class ResetPasswordMismatchException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2386609994185273078L;

	public ResetPasswordMismatchException() {
		super();
	}

	public ResetPasswordMismatchException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public ResetPasswordMismatchException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public ResetPasswordMismatchException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public ResetPasswordMismatchException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public ResetPasswordMismatchException(IErrorObject errorObject) {
		super(errorObject);
	}

	public ResetPasswordMismatchException(String errorMessage) {
		super(errorMessage);
	}

	public ResetPasswordMismatchException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}

