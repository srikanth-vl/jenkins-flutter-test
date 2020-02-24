package com.vassarlabs.common.utils.err;

public abstract class VLException extends Throwable {

	private static final long serialVersionUID = 1L;

	protected IErrorObject errorObject;

	public VLException() {
		super();
		this.errorObject = new EmptyErrorObject();
	}
	
	public VLException(IErrorObject errorObject) {
		super();
		this.errorObject = errorObject;
	}
	
	public VLException(String errorMessage) {
		super(errorMessage);
		errorObject = new SimpleErrorObject(errorMessage);
	}
	
	public VLException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
		errorObject = new SimpleErrorObject(errorMessage);
	}

	public VLException(IErrorObject errorObject, String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.errorObject = errorObject;
	}

	public VLException(IErrorObject errorObject, String message, Throwable cause) {
		super(message, cause);
		this.errorObject = errorObject;
	}
	
	public VLException(IErrorObject errorObject, String message) {
		super(message);
		this.errorObject = errorObject;
	}
	
	public VLException(IErrorObject errorObject, Throwable cause) {
		super(cause);
		this.errorObject = errorObject;
	}
	
	public IErrorObject getErrorObject() {
		return errorObject;
	}

	public void setErrorObject(IErrorObject errorObject) {
		this.errorObject = errorObject;
	}

}