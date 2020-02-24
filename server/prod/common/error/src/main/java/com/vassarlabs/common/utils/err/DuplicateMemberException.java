package com.vassarlabs.common.utils.err;


public class DuplicateMemberException extends VLException {

	private static final long serialVersionUID = -2208064753925747958L;

	public DuplicateMemberException() {
		super();
	}

	public DuplicateMemberException(IErrorObject errorObject, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(errorObject, message, cause, enableSuppression, writableStackTrace);
	}

	public DuplicateMemberException(IErrorObject errorObject, String message, Throwable cause) {
		super(errorObject, message, cause);
	}

	public DuplicateMemberException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public DuplicateMemberException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public DuplicateMemberException(IErrorObject errorObject) {
		super(errorObject);
	}

	public DuplicateMemberException(String errorMessage) {
		super(errorMessage);
	}
	
	public DuplicateMemberException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
