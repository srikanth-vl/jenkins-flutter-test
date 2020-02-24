package com.vassarlabs.prod.sms.service.api;

import com.vassarlabs.common.utils.err.SMSSendException;

public interface ISMSService {

	int sendMessage(String smsCountryUrl, String smsCountryUsername, String smsCountryPassword,  long mobileNumber, String message) 
			throws SMSSendException;

}
