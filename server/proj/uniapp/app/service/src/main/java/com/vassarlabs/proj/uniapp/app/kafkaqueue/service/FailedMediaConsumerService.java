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
import com.vassarlabs.proj.uniapp.api.pojo.FormMediaValue;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueMediaService;
import com.vassarlabs.proj.uniapp.app.relay.service.FormMediaRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class FailedMediaConsumerService 
implements Runnable {

	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;

	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;

	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
	private String maxPollRecords;

	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;
	
	@Value(value = KafkaConstants.FAILED_MEDIA_TOPIC)
	private String mediaTopic;


	@Autowired private ConsumerCreator consumerCreator;
	@Autowired private FormMediaRelayService imageGeotagRelayService;
	@Autowired private ISendToQueueMediaService sendToQueueMediaService;

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
		final Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  maxPollRecords, Arrays.asList(mediaTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - Start Poll");
		
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(6000);

			Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
			if(consumerRecords == null || consumerRecords.isEmpty()) {
				logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - No record found");
				break;
			}
	
			try {
				records = new ArrayList<>();
				
				for (ConsumerRecord<String, String> record: consumerRecords) {

					logger.debug("In FailedMediaConsumerService ::  getAndProcessRecords() : Record Offset - " + record.offset() + " Topic - " + record.topic() + " Partition - " + record.partition());	
					records.add(record.value());

					TopicPartition topicPartition  = new TopicPartition(record.topic(),record.partition());
					OffsetAndMetadata offsetAndMetadata = new OffsetAndMetadata(record.offset()+1);
					
					if(record.topic().equals(mediaTopic)) {
						
						FormMediaValue mediaData = mapper.readValue(record.value().toString(),FormMediaValue.class );
						logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - ImageId: "  + mediaData.getMediaUUID());
						
						List<ServiceOutputObject> outputList =  imageGeotagRelayService.relayMediaToExternalServer(mediaData);
						
						if(outputList != null && !outputList.isEmpty() && outputList.get(0).isSuccessful()) {
							logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - Relay Successful");
							offsets.put(topicPartition , offsetAndMetadata);
						} 
						else {
							failed = true;
							sendToQueueMediaService.sendMessage(mediaData, mediaTopic);
							// consumer.seek(new TopicPartition(record.topic(), record.partition()), record.offset());
							// break;
						}
					} else {
						logger.debug("In FailedMediaConsumerService ::  getAndProcessRecords() : Record Offset - " + record.offset() + " Topic - " + record.topic() + " Partition - " + record.partition());
					}

				}
				logger.debug("In FailedMediaConsumerService :: getAndProcessRecords() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception e) {
				logger.debug("In FailedMediaConsumerService :: getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - Commit offsets ");
			consumer.commitAsync();
			if(failed) {
				logger.info("In FailedMediaConsumerService :: getAndProcessRecords() - Failed to process data , Closing poll");
				break;
			}		
		}
		consumer.close();
	}
}
