package com.vassarlabs.common.utils.err;

public class FormSubmissionSyncPeriodExceedException extends VLException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6096564737713705399L;

	/**
	 * 
	 */
	public FormSubmissionSyncPeriodExceedException() {
		super();
	}
	
	public FormSubmissionSyncPeriodExceedException(IErrorObject errorObject, String message,
			Throwable cause) {
		super(errorObject, message, cause);
	}

	public FormSubmissionSyncPeriodExceedException(IErrorObject errorObject, String message) {
		super(errorObject, message);
	}

	public FormSubmissionSyncPeriodExceedException(IErrorObject errorObject, Throwable cause) {
		super(errorObject, cause);
	}

	public FormSubmissionSyncPeriodExceedException(IErrorObject errorObject) {
		super(errorObject);
	}

	public FormSubmissionSyncPeriodExceedException(String errorMessage) {
		super(errorMessage);
	}
	
	public FormSubmissionSyncPeriodExceedException(String errorMessage, Throwable throwable) {
		super(errorMessage, throwable);
	}

}
