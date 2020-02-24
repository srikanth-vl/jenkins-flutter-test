package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.vassarlabs.proj.uniapp.application.properties.load.ApplicationProperties;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@Component
public class CreateKafkaConsumers {
	@Autowired 
	private DataConsumerService dataComsumerService;
	@Autowired 
	private MediaConsumerService mediaComsumerService;
	@Autowired 
	private FailedDataConsumerService failedDataConsumerService;
	@Autowired 
	private FailedMediaConsumerService failedMediaConsumerService;
	@Autowired 
	private FailedDataSubmissionConsumerService failedDataSubmissionConsumerService;
	@Autowired
	private BboxConsumerService bboxConsumerService;
	@Autowired
	private UserDataConsumerService userDataConsumerService;
	@Autowired 
	private ApplicationProperties properties;
	
	public void createConsumersForPrimaryQueue() {
		String noOfConsumers = properties.getProperty(KafkaConstants.KAFKA_CONSUMER_COUNT);
		for (int i = 0 ; i < Integer.parseInt(noOfConsumers); i++) {
			Thread dataComsumerThread = new Thread(dataComsumerService);
			dataComsumerThread.start();
			Thread failedSubmissiomDataComsumerThread = new Thread(failedDataSubmissionConsumerService);
			failedSubmissiomDataComsumerThread.start();
			Thread mediaComsumerThread = new Thread(mediaComsumerService);
			mediaComsumerThread.start();
			Thread bboxComsumerThread = new Thread(bboxConsumerService);
			bboxComsumerThread.start();
			Thread userDataConsumerThread = new Thread(userDataConsumerService);
			userDataConsumerThread.start();
		}
//		Thread bboxComsumerThread = new Thread(bboxConsumerService);
//		bboxComsumerThread.start();
	}
	public void createConsumersForFailedQueue() {
		String noOfConsumers = properties.getProperty(KafkaConstants.KAFKA_CONSUMER_COUNT);
		for (int i = 0 ; i < 1; i++) {
			Thread dataComsumerThread = new Thread(failedDataConsumerService);
			dataComsumerThread.start();
			Thread mediaComsumerThread = new Thread(failedMediaConsumerService);
			mediaComsumerThread.start();
		}
	}
}
