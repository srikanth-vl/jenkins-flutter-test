package com.vassarlabs.common.utils.err;

public class GenericVLException
	extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1610027640668887750L;

	public GenericVLException() {
		super();
	}

	public GenericVLException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public GenericVLException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public GenericVLException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public GenericVLException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public GenericVLException(IErrorObject errorObject) {
		super(errorObject);
	}

	public GenericVLException(String errorMessage) {
		super(errorMessage);
	}
	
	public GenericVLException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}