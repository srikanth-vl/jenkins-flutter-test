package com.vassarlabs.common.utils.err;


public class DataDeletionException
	extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3889914400461206582L;

	public DataDeletionException() {
		super();
	}

	public DataDeletionException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DataDeletionException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public DataDeletionException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DataDeletionException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DataDeletionException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DataDeletionException(String errorMessage) {
		super(errorMessage);
	}
	
	public DataDeletionException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
