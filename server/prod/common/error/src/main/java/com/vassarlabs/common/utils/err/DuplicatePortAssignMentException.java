package com.vassarlabs.common.utils.err;

import com.vassarlabs.common.utils.err.IErrorObject;

public class DuplicatePortAssignMentException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3541486901702685705L;
	
	public DuplicatePortAssignMentException() {
		super();
	}

	public DuplicatePortAssignMentException(IErrorObject errorObject, String message,
			Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DuplicatePortAssignMentException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public DuplicatePortAssignMentException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DuplicatePortAssignMentException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DuplicatePortAssignMentException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DuplicatePortAssignMentException(String errorMessage) {
		super(errorMessage);
	}
	
	public DuplicatePortAssignMentException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
