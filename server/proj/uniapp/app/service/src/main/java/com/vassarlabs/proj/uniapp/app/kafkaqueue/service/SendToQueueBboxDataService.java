package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.BboxContainerObject;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;
@Component
@Configuration
@PropertySource("classpath:kafka.properties")
public class SendToQueueBboxDataService {
	@Value(value = KafkaConstants.MAP_BBOX_DATA_TOPIC)
	private String topic;
	@Autowired
	private IVLLogService logFactory;

	private IVLLogger logger;

	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	@Autowired
	private KafkaTemplate<Long, Object> kafkaTemplate;
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	public void sendMessage(BboxContainerObject bbObject) throws JsonProcessingException {
		logger.info("In SendToQueueMediaService :: sendMessage()- Sending to Media Queue :" + topic);
		
		String submittedObjectJsonFormat = objectMapper.writeValueAsString(bbObject);
		ListenableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic,bbObject);
        future.addCallback(new ListenableFutureCallback<SendResult<Long, Object>>() {
        	
            @Override
            public void onSuccess(final SendResult<Long, Object> message) {
            	logger.info("sent message= " + message + " with offset= " + message.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(final Throwable throwable) {
            	List<String> errorList = new ArrayList<>();
        		errorList.add("Failed to send message to Queue");
            	
            	logger.error("unable to send message= " + submittedObjectJsonFormat, throwable);
            }
        });
		
		return;
	}
}
