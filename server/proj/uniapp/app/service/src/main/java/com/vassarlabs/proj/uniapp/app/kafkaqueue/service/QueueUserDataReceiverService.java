package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserData;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class QueueUserDataReceiverService {
	
	@Value(value = KafkaConstants.USER_META_DATA_TOPIC)
	private String dataTopic;

	@Autowired 
	private SendToQueueUserDataService sendToQueueUserDataService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public List<ServiceOutputObject> execute(UserData userDataObject) throws JsonProcessingException{
		return sendToQueueUserDataService.sendMessage(userDataObject, dataTopic);
	
	}
}
