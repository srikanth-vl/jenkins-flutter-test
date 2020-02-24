package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.IQueueDataReceiverService;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueDataService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
@Component("QueueDataReceiverService")
public class QueueDataReceiverService
	implements IQueueDataReceiverService {
	
	@Value(value = KafkaConstants.DATA_TOPIC)
	private String dataTopic;

	@Autowired 
	private ISendToQueueDataService sendToQueueDataService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public List<ServiceOutputObject> execute(AppFormDataSubmittedList appFormDataSubmittedList) throws JsonProcessingException{
		return sendToQueueDataService.sendMessage(appFormDataSubmittedList, dataTopic);
	
	}
}
