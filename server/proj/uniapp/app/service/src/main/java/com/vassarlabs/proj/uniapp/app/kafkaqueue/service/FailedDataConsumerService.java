package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.kafka.common.ConsumerCreator;
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueDataService;
import com.vassarlabs.proj.uniapp.app.relay.service.FormDataRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class FailedDataConsumerService  
	implements Runnable{
	
	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;
	
	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;
	
	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
	private String maxPollRecords;
	
	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;

	@Value(value = KafkaConstants.FAILED_DATA_TOPIC)
	private String dataTopic;
	
	@Autowired 
	private ConsumerCreator consumerCreator;
	@Autowired 
	private FormDataRelayService formDataRelayService;
	@Autowired 
	private ISendToQueueDataService sendToQueueDataService;
	
	@Autowired 
	private IVLLogService logFactory;
	private IVLLogger logger;
	@PostConstruct
	public void init() {
		logger = logFactory.getLogger(getClass());
	}
	
	ObjectMapper mapper = new ObjectMapper();	
	
	@Override
	public void run() {
		getAndProcessRecords(); 
	}
	
	public void getAndProcessRecords() {
		boolean failed = false;
		final Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  maxPollRecords, Arrays.asList(dataTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In FailedDataConsumerService :: getAndProcessRecords() - Start Poll");
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(6000);
			
			Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
			if(consumerRecords == null) {
				logger.info("In FailedDataConsumerService :: getAndProcessRecords() - No record found");
				break;
			}
			if(consumerRecords.isEmpty()) {
				logger.info("In FailedDataConsumerService :: getAndProcessRecords() - No record found");
				break;
			}
			
			try {
				records = new ArrayList<>();
				for (ConsumerRecord<String, String> record: consumerRecords) {
					
					System.out.println(record.offset());
					System.out.println(record.topic());
					records.add(record.value());
					TopicPartition topicPartition  = new TopicPartition(record.topic(), record.partition());
					OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset() + 1);
					if(record.topic().equals(dataTopic)) {
						AppFormDataSubmittedList submittedDataObject = mapper.readValue(record.value().toString(),AppFormDataSubmittedList.class );
						//TODO :: Refactor - change from list to object
						List<ServiceOutputObject> outputList =  formDataRelayService.relayDataToExternalServer(submittedDataObject);
						logger.info(String.valueOf(outputList.get(0).isSuccessful()));
						if(!outputList.isEmpty() && outputList.get(0).isSuccessful()) {
							logger.info("FailedDataConsumerService ::getAndProcessRecords() - Relay Successful");
							offsets.put(topicPartition , offsetAndMetadata);
						}
						else {
							failed = true;
							int relayRetries = submittedDataObject.getRelayRetries() == null ? 1: submittedDataObject.getRelayRetries() + 1 ;
							submittedDataObject.setRelayRetries(relayRetries);
							sendToQueueDataService.sendMessage(submittedDataObject, dataTopic);
		                }
					
					} else {
						System.out.println(record.offset());
						System.out.println(record.topic());
					}
				   
				}
				logger.debug("In FailedDataConsumerService :: getAndProcessRecords() - run() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception  e) {
				logger.debug("In FailedDataConsumerService :: getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In FailedDataConsumerService :: getAndProcessRecords() - Commit offsets ");
			consumer.commitAsync();
			if(failed) {
				logger.info("In FailedDataConsumerService :: getAndProcessRecords() - Failed to process data , Closing poll");
				break;
			}
		}
		consumer.close();
		
	}
}
