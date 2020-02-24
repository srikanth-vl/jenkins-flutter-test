package com.vassarlabs.common.utils.err;

public class DeletionNotAllowedException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3235229317281307002L;
	
	public DeletionNotAllowedException() {
		super();
	}

	public DeletionNotAllowedException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DeletionNotAllowedException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public DeletionNotAllowedException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DeletionNotAllowedException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DeletionNotAllowedException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DeletionNotAllowedException(String errorMessage) {
		super(errorMessage);
	}
	
	public DeletionNotAllowedException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
