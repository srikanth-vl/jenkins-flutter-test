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
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.api.pojo.UserData;
import com.vassarlabs.proj.uniapp.app.relay.service.UserDataRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class UserDataConsumerService  
implements Runnable{
	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;

	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;

	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
	private String maxPollRecords;

	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;

	@Value(value = KafkaConstants.USER_META_DATA_TOPIC)
	private String userDataTopic;

	
	@Autowired ConsumerCreator consumerCreator;
	@Autowired UserDataRelayService userDataRelayService;
	@Autowired SendToQueueUserDataService sendToQueueDataService;

	
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
		Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  maxPollRecords, Arrays.asList(userDataTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In UserDataConsumerService ::getAndProcessRecords()- Start Poll");
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(Long.MAX_VALUE);
			if(consumerRecords == null) {
				logger.info("In UserDataConsumerService ::getAndProcessRecords() - No record found");
				break;
			}

			if(consumerRecords.isEmpty()) {
				logger.debug("In UserDataConsumerService ::getAndProcessRecords() -No record found");
				continue;
			}

			try {
				records = new ArrayList<>();
				for (ConsumerRecord<String, String> record: consumerRecords) {
					System.out.println(record.offset());
					System.out.println(record.topic());
					records.add(record.value());
					if(record.topic().equals(userDataTopic)) {
						UserData userDataObject = mapper.readValue(record.value().toString(),UserData.class );
						if(userDataObject == null) {
							logger.info("In UserDataConsumerService ::getAndProcessRecords() -  Submitted form data is null for "+ record);
							continue;
						}

						List<ServiceOutputObject> outputList =  userDataRelayService.relayDataToExternalServer(userDataObject);
						if(!outputList.isEmpty() && outputList.get(0).isSuccessful()) {
							logger.info("In UserDataConsumerService ::getAndProcessRecords() - Relay Successful");
						}
						else {
							logger.info("In UserDataConsumerService ::getAndProcessRecords() : failed to relay data , sending to " + userDataTopic);
							sendToQueueDataService.sendMessage(userDataObject, userDataTopic);	
						}
					} 
				}

				logger.debug("In UserDataConsumerService ::getAndProcessRecords() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception e) {
				logger.debug("In UserDataConsumerService ::getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In UserDataConsumerService ::getAndProcessRecords()- Commit start" );
			consumer.commitAsync();
			logger.info("In UserDataConsumerService ::getAndProcessRecords()- Commit done" );
		}

		consumer.close();

	}

}
