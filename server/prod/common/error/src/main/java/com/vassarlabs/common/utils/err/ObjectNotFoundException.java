package com.vassarlabs.common.utils.err;

public class ObjectNotFoundException
	extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3889914400461206582L;

	public ObjectNotFoundException() {
		super();
	}

	public ObjectNotFoundException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public ObjectNotFoundException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public ObjectNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public ObjectNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public ObjectNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public ObjectNotFoundException(String errorMessage) {
		super(errorMessage);
	}
	
	public ObjectNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
