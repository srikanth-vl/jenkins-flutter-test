package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.IMediaReceiverService;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueMediaService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;
@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
@Component("QueueMediaReceiverService")
public class QueueMediaReceiverService
	implements IMediaReceiverService {
	
	@Value(value = KafkaConstants.MEDIA_TOPIC)
	private String mediaTopic;

	@Autowired ISendToQueueMediaService sendToQueueMediaService;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public ServiceOutputObject execute(FormMediaValue formMediaValue) throws JsonProcessingException {
		return sendToQueueMediaService.sendMessage(formMediaValue, mediaTopic);

	}

}
