package com.vassarlabs.proj.uniapp.app.insert.service;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;

public interface ISendToQueueDataService {
	/**
	 * Receive submitted text data, and send it to the given topic
	 * 
	 * @param appFormDataSubmittedList
	 * @param topic
	 * @throws JsonProcessingException 
	 */
	
	public List<ServiceOutputObject> sendMessage(AppFormDataSubmittedList appFormDataSubmittedList, String topic) throws JsonProcessingException;

}
