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
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserTrackingObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueDataService;
import com.vassarlabs.proj.uniapp.enums.APITypes;
@Component
public class SendToQueueDataService 
	implements ISendToQueueDataService {
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
	
	public List<ServiceOutputObject> sendMessage(AppFormDataSubmittedList appFormDataSubmittedList, String topic)
		throws JsonProcessingException {
		
		logger.debug("In SendToQueueDataService :: sendMessage()- Sending to Queue :" + topic);
		
		String submittedObjectJsonFormat = objectMapper.writeValueAsString(appFormDataSubmittedList);
		List<ServiceOutputObject> outputList = new ArrayList<ServiceOutputObject>();
		ServiceOutputObject outputObject = new ServiceOutputObject();
		UserTrackingObject trackingObject = new UserTrackingObject(appFormDataSubmittedList.getSuperAppId()
				, appFormDataSubmittedList.getAppId(), appFormDataSubmittedList.getUserId(), appFormDataSubmittedList.getTokenId(),
				APITypes.SEND_TO_QUEUE, "Send FormTextData to Queue Topic: "+ topic, submittedObjectJsonFormat, null, false, appFormDataSubmittedList.getAppFormDataList().get(0).getTimeStamp());
		
		ListenableFuture<SendResult<Long, Object>> future = kafkaTemplate.send(topic,appFormDataSubmittedList);
		
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
        		errorList.add("Failed to send message to Queue : Topic - " + topic + " -- AppFormDataSubmittedList : " + appFormDataSubmittedList);
            	trackingObject.setRequestSuccessful(false);
            	outputObject.setSuccessful(false);
            	logger.error("Failed to send message to Queue : Topic - " + topic + " -- AppFormDataSubmittedList : " + appFormDataSubmittedList);
            	logger.error("Unable to send message= " + submittedObjectJsonFormat, throwable);
            }
        });
		outputObject.setTrackingObject(trackingObject);
		outputList.add(outputObject);
		return outputList;
	}
}
