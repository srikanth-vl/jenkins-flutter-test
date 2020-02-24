package com.vassarlabs.common.utils.err;

public class TokenExpiredException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2722597259495415862L;
	public TokenExpiredException() {
		super();
	}
	
	public TokenExpiredException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public TokenExpiredException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public TokenExpiredException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public TokenExpiredException(IErrorObject errorObject) {
		super(errorObject);
	}

	public TokenExpiredException(String errorMessage) {
		super(errorMessage);
	}
	
	public TokenExpiredException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
