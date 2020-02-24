package com.vassarlabs.common.utils.err;

public class InvalidEmailAddressException extends VLException {

	private static final long serialVersionUID = 5050104008729162936L;

	public InvalidEmailAddressException() {
		super();
	}

	public InvalidEmailAddressException(IErrorObject errorObject, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public InvalidEmailAddressException(IErrorObject errorObject, String message, Throwable cause) {
		super(errorObject, message, cause);
	}

	public InvalidEmailAddressException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public InvalidEmailAddressException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public InvalidEmailAddressException(IErrorObject errorObject) {
		super(errorObject);
	}

	public InvalidEmailAddressException(String errorMessage) {
		super(errorMessage);
	}
	
	public InvalidEmailAddressException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
