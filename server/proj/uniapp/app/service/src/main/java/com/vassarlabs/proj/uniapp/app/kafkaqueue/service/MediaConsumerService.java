package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vassarlabs.prod.common.logging.api.IVLLogService;
import com.vassarlabs.prod.common.logging.api.IVLLogger;
import com.vassarlabs.prod.kafka.common.ConsumerCreator;
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueMediaService;
import com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service.FormMediaProcessingService;
import com.vassarlabs.proj.uniapp.app.relay.service.FormMediaRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@PropertySource("classpath:kafka.properties")
@Configuration
public class MediaConsumerService  
	implements Runnable{
	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;
	
	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;
	
	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
	private String maxPollRecords;
	
	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;

	@Value(value = KafkaConstants.MEDIA_TOPIC)
	private String mediaTopic;
	
	@Value(value = KafkaConstants.FAILED_MEDIA_TOPIC)
	private String failedMediaTopic;
	
	@Autowired 
	private ConsumerCreator consumerCreator;
	
	@Autowired
	private FormMediaRelayService imageGeotagRelayService;
	
	@Autowired 
	private FormMediaProcessingService imageSubmittedAndReceivedDataProcessingService;

	@Autowired 
	private ISendToQueueMediaService sendToQueueMediaService;
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
		Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  maxPollRecords, Arrays.asList(mediaTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In MediaConsumerService ::getAndProcessRecords() - Start Poll ");
		
		while (true) {
			
			ConsumerRecords<String, String> consumerRecords = consumer.poll(Long.MAX_VALUE);
			if(consumerRecords == null || consumerRecords.isEmpty()) {
				logger.info("In MediaConsumerService ::getAndProcessRecords() - No record found");
				break;
			}
			
			try {
				records = new ArrayList<>();
				for (ConsumerRecord<String, String> record: consumerRecords) {
					
					logger.debug("In MediaConsumerService ::  getAndProcessRecords() : Record Offset - " + record.offset() + " Topic - " + record.topic() + " Partition - " + record.partition());	
					records.add(record.value());
					
					if(record.topic().equals(mediaTopic)) {
						
						FormMediaValue mediaData = mapper.readValue(record.value().toString(),FormMediaValue.class);
						if(mediaData == null) {
							logger.info("In MediaConsumerService ::getAndProcessRecords() - Submitted form media value is null for "+ record);
							continue;
						}
						
						// Process submitted and received images data -- Picks up data of received images  and processes it
						imageSubmittedAndReceivedDataProcessingService.updateReceivedImageData(mediaData);
						mediaData.setMediaContent(null);
						// Relay to external server
						List<ServiceOutputObject> outputList =  imageGeotagRelayService.relayMediaToExternalServer(mediaData);
						if(outputList != null && !outputList.isEmpty() && outputList.get(0).isSuccessful()) {
							logger.info("In MediaConsumerService ::getAndProcessRecords() - Relay Successful");
						} 
						else {
							logger.info("In MediaConsumerService ::getAndProcessRecords() - failed to relay media Value , sending to " + failedMediaTopic);
							sendToQueueMediaService.sendMessage(mediaData, failedMediaTopic);
		                }
					
					}
				}
				logger.debug("In MediaConsumerService ::getAndProcessRecords() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception  e) {
				logger.debug("In MediaConsumerService ::getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In MediaConsumerService ::getAndProcessRecords() - Commit  start" );
			consumer.commitAsync();
			logger.info("In MediaConsumerService ::getAndProcessRecords() - Commit done" );
		}
		consumer.close();
	}
}
