package com.vassarlabs.common.utils.err;

public class UnsupportedInputException extends VLException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public UnsupportedInputException(String message) {
		super(message);
	}
	
	public UnsupportedInputException(Exception e) {
		super(e.getMessage());
	}
}
