package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueMediaService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
@Component
public class SendToQueueMediaService 
	implements ISendToQueueMediaService {

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
	
	public ServiceOutputObject sendMessage(FormMediaValue formMediaValue, String topic) throws JsonProcessingException {
		logger.info("In SendToQueueMediaService :: sendMessage()- Sending to Media Queue :" + topic);
		formMediaValue.setMediaContent(null);
		ServiceOutputObject outputObject = new ServiceOutputObject();
		String submittedObjectJsonFormat = objectMapper.writeValueAsString(formMediaValue);
		UserTrackingObject trackingObject = new UserTrackingObject(formMediaValue.getSuperAppId(), formMediaValue.getAppId(), formMediaValue.getUserId(), formMediaValue.getTokenId(),
				APITypes.SEND_TO_QUEUE, "Send FormMediaValue to Queue Topic: "+ topic, submittedObjectJsonFormat, null, false, formMediaValue.getSyncTimeStamp());
		ListenableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic,formMediaValue);
        future.addCallback(new ListenableFutureCallback<SendResult<Long, Object>>() {
        	
            @Override
            public void onSuccess(final SendResult<Long, Object> message) {
            	logger.info("sent message= " + message + " with offset= " + message.getRecordMetadata().offset());
            	trackingObject.setRequestSuccessful(true);
            	outputObject.setSuccessful(true);
        		
            }

            @Override
            public void onFailure(final Throwable throwable) {
            	List<String> errorList = new ArrayList<>();
        		errorList.add("Failed to send message to Queue");
            	trackingObject.setRequestSuccessful(false);
            	outputObject.setSuccessful(false);
            	logger.error("unable to send message= " + submittedObjectJsonFormat, throwable);
            }
        });
		outputObject.setTrackingObject(trackingObject);
		return outputObject;
	}
}
