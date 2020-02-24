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
import com.vassarlabs.proj.uniapp.api.pojo.AppFormDataSubmittedList;
import com.vassarlabs.proj.uniapp.api.pojo.ServiceOutputObject;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueDataService;
import com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service.FormMediaProcessingService;
import com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service.ProjectSubmissionCountUpdateService;
import com.vassarlabs.proj.uniapp.app.relay.service.FormDataRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class DataConsumerService  
implements Runnable{
	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;

	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;

	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
	private String maxPollRecords;

	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;

	@Value(value = KafkaConstants.DATA_TOPIC)
	private String dataTopic;
	
	@Value(value = KafkaConstants.FAILED_DATA_TOPIC)
	private String failedDataTopic;


	@Autowired ConsumerCreator consumerCreator;
	@Autowired FormDataRelayService formDataRelayService;
	@Autowired FormMediaProcessingService imageSubmittedAndReceivedDataProcessingService;
	@Autowired ISendToQueueDataService sendToQueueDataService;

	@Autowired 
	private ProjectSubmissionCountUpdateService projectSubmissionCountUpdateService;
	
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
		Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  maxPollRecords, Arrays.asList(dataTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In DataConsumerService ::getAndProcessRecords()- Start Poll");
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(Long.MAX_VALUE);
			if(consumerRecords == null) {
				logger.info("In DataConsumerService ::getAndProcessRecords() - No record found");
				break;
			}

			if(consumerRecords.isEmpty()) {
				logger.debug("In DataConsumerService ::getAndProcessRecords() -No record found");
				continue;
			}

			try {
				records = new ArrayList<>();
				for (ConsumerRecord<String, String> record: consumerRecords) {
					System.out.println(record.offset());
					System.out.println(record.topic());
					records.add(record.value());
					if(record.topic().equals(dataTopic)) {
						AppFormDataSubmittedList appFormDataSubmittedListObject = mapper.readValue(record.value().toString(),AppFormDataSubmittedList.class );
						if(appFormDataSubmittedListObject == null) {
							logger.info("In DataConsumerService ::getAndProcessRecords() -  Submitted form data is null for "+ record);
							continue;
						}
						projectSubmissionCountUpdateService.execute(appFormDataSubmittedListObject, true);
						/**
						 * Process submitted and received images data -- Picks up data of submitted
						 * and received images from form and processes it
						 * 						 *
						 */
						imageSubmittedAndReceivedDataProcessingService.updateSubmittedImageData(appFormDataSubmittedListObject);
						/**
						 * 	Relay to external server
						 * 
						 */
						List<ServiceOutputObject> outputList =  formDataRelayService.relayDataToExternalServer(appFormDataSubmittedListObject);
						if(!outputList.isEmpty() && outputList.get(0).isSuccessful()) {
							logger.info("In DataConsumerService ::getAndProcessRecords() - Relay Successful");
						}
						else {
							logger.info("In DataConsumerService ::getAndProcessRecords() : failed to relay data , sending to " + failedDataTopic);
							sendToQueueDataService.sendMessage(appFormDataSubmittedListObject, failedDataTopic);						}

					} 
				}

				logger.debug("In DataConsumerService ::getAndProcessRecords() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception e) {
				logger.debug("In DataConsumerService ::getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In DataConsumerService ::getAndProcessRecords()- Commit start" );
			consumer.commitAsync();
			logger.info("In DataConsumerService ::getAndProcessRecords()- Commit done" );
		}

		consumer.close();

	}

}
