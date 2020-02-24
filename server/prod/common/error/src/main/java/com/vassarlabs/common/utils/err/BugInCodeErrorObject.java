package com.vassarlabs.common.utils.err;

import com.vassarlabs.prod.common.utils.StringUtils;

public class BugInCodeErrorObject
	extends ErrorObject {
	
	public BugInCodeErrorObject(String errorMessage) {
		this.errorCode = IErrorObject.BUG_IN_CODE_ERROR_CODE;
		this.errorType = StringUtils.EMPTY_STRING;
		this.errorMessage = errorMessage;
	}
}
