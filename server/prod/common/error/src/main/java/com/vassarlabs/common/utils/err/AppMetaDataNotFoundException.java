package com.vassarlabs.common.utils.err;

public class AppMetaDataNotFoundException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6001844829575364340L;
	public AppMetaDataNotFoundException() {
		super();
	}
	
	public AppMetaDataNotFoundException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public AppMetaDataNotFoundException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public AppMetaDataNotFoundException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public AppMetaDataNotFoundException(IErrorObject errorObject) {
		super(errorObject);
	}

	public AppMetaDataNotFoundException(String errorMessage) {
		super(errorMessage);
	}
	
	public AppMetaDataNotFoundException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}
}
