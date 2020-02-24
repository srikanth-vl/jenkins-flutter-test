package com.vassarlabs.common.utils.err;

import com.vassarlabs.prod.common.utils.StringUtils;

public class SimpleErrorObject extends ErrorObject {

	public SimpleErrorObject(String errorMessage) {
		super(StringUtils.EMPTY_STRING, IErrorObject.EMPTY_ERROR_CODE, errorMessage, NO_ERROR);
	}
}
