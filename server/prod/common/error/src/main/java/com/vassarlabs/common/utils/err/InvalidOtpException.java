package com.vassarlabs.common.utils.err;

public class InvalidOtpException extends VLException {


	/**
	 * 
	 */
	private static final long serialVersionUID = 2816366708164208122L;

	public InvalidOtpException() {
		super();
	}

	public InvalidOtpException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidOtpException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public InvalidOtpException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public InvalidOtpException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public InvalidOtpException(IErrorObject errorObject) {
		super(errorObject);
	}

	public InvalidOtpException(String errorMessage) {
		super(errorMessage);
	}

	public InvalidOtpException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
