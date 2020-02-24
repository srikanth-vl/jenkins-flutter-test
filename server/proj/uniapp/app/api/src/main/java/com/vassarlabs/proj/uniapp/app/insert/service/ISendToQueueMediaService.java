package com.vassarlabs.proj.uniapp.app.insert.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface ISendToQueueMediaService {
	/**
	 * Receive media data, and send it to the given topic
	 * 
	 * @param formMediaValue
	 * @param topic
	 * @throws JsonProcessingException 
	 */
	public ServiceOutputObject sendMessage(FormMediaValue formMediaValue, String topic) throws JsonProcessingException;

}
