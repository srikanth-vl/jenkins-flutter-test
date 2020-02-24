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
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserData;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.enums.APITypes;
@Component
public class SendToQueueUserDataService {
	
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
	
	public List<ServiceOutputObject> sendMessage(UserData userDataObject, String topic)
		throws JsonProcessingException {
		
		logger.debug("In SendToQueueUserDataService :: sendMessage()- Sending to Queue :" + topic);
		
		String submittedObjectJsonFormat = objectMapper.writeValueAsString(userDataObject);
		List<ServiceOutputObject> outputList = new ArrayList<ServiceOutputObject>();
		ServiceOutputObject outputObject = new ServiceOutputObject();
		UserTrackingObject trackingObject = new UserTrackingObject(userDataObject.getSuperAppId()
				, UUIDUtils.getDefaultUUID(), userDataObject.getUserId(), UUIDUtils.getDefaultUUID(),
				APITypes.SEND_TO_QUEUE, "Send UserData to Queue Topic: "+ topic, submittedObjectJsonFormat, null, false, System.currentTimeMillis());
		
		ListenableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic,userDataObject);
		
		future.addCallback(new ListenableFutureCallback<SendResult<Long, Object>>() {

            @Override
            public void onSuccess(final SendResult<Long, Object> message) {
            	trackingObject.setRequestSuccessful(true);
            	outputObject.setSuccessful(true);
            	logger.debug("Sent message= " + message + " with offset= " + message.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(final Throwable throwable) {
            	List<String> errorList = new ArrayList<>();
        		errorList.add("Failed to send message to Queue : Topic - " + topic + " -- UserData : " + userDataObject);
            	trackingObject.setRequestSuccessful(false);
            	outputObject.setSuccessful(false);
            	logger.error("Failed to send message to Queue : Topic - " + topic + " -- UserData : " + userDataObject);
            	logger.error("Unable to send message= " + submittedObjectJsonFormat, throwable);
            }
        });
		outputObject.setTrackingObject(trackingObject);
		outputList.add(outputObject);
		return outputList;
	}
}
