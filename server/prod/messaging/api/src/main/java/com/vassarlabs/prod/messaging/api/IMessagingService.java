package com.vassarlabs.prod.messaging.api;

import org.springframework.mail.MailException;

import com.vassarlabs.common.utils.err.InvalidInputParamException;
import com.vassarlabs.prod.messaging.model.Message;

public interface IMessagingService {
	
	public boolean sendMessage(Message msg, boolean hasAttachment) 
		throws InvalidInputParamException, MailException;
}
