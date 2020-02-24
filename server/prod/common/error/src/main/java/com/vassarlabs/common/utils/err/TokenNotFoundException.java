package com.vassarlabs.common.utils.err;

public class TokenNotFoundException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7799817387033074790L;
	public TokenNotFoundException() {
		super();
	}
	
	public TokenNotFoundException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public TokenNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public TokenNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public TokenNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public TokenNotFoundException(String errorMessage) {
		super(errorMessage);
	}
	
	public TokenNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
