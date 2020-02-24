package com.vassarlabs.common.utils.err;

public class InsufficientDataException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public InsufficientDataException(String message) {
		super(message);
	}
	
	public InsufficientDataException(Exception e) {
		super(e.getMessage());
	}
}
