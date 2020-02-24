package com.vassarlabs.common.utils.err;

public class AppVersionMismatchException 
	extends VLException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3399152282604731967L;

	public AppVersionMismatchException() {
		super();
	}

	public AppVersionMismatchException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public AppVersionMismatchException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public AppVersionMismatchException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public AppVersionMismatchException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public AppVersionMismatchException(IErrorObject errorObject) {
		super(errorObject);
	}

	public AppVersionMismatchException(String errorMessage) {
		super(errorMessage);
	}
	
	public AppVersionMismatchException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
