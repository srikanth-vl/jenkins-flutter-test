package com.vassarlabs.common.utils.err;

import com.vassarlabs.prod.common.utils.StringUtils;

public class EmptyErrorObject extends ErrorObject {

	public EmptyErrorObject() {
		super(StringUtils.EMPTY_STRING, IErrorObject.EMPTY_ERROR_CODE, StringUtils.EMPTY_STRING, NO_ERROR);
	}
}
