package com.vassarlabs.proj.uniapp.app.kafkaqueue.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

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
import com.vassarlabs.prod.common.utils.UUIDUtils;
import com.vassarlabs.prod.kafka.common.ConsumerCreator;
import com.vassarlabs.proj.uniapp.api.pojo.BboxContainerObject;
import com.vassarlabs.proj.uniapp.api.pojo.MapConfig;
import com.vassarlabs.proj.uniapp.app.insert.service.ISendToQueueDataService;
import com.vassarlabs.proj.uniapp.app.kafka.recordprocessor.service.FormMediaProcessingService;
import com.vassarlabs.proj.uniapp.app.relay.service.FormDataRelayService;
import com.vassarlabs.proj.uniapp.constants.KafkaConstants;
import com.vassarlabs.proj.uniapp.constants.MapConstants;
import com.vassarlabs.proj.uniapp.crud.service.MapConfigDataCrudService;
import com.vassarlabs.proj.uniapp.map.osmdata.TileProviderUtil;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class BboxConsumerService  
implements Runnable{
	@Value(value = KafkaConstants.KAFKA_BROKERS)
	private String bootstrapAddress;

	@Value(value = KafkaConstants.KAFKA_GROUP_ID_CONFIG)
	private String groupIdConfig;

//	@Value(value = KafkaConstants.MAX_POLL_RECORDS)
//	private String maxPollRecords;

	@Value(value = KafkaConstants.OFFSET_RESET_EARLIER)
	private String offsetResetEarliest;

	@Value(value = KafkaConstants.MAP_BBOX_DATA_TOPIC)
	private String bboxDataTopic;
	
	@Value(value = KafkaConstants.FAILED_DATA_TOPIC)
	private String failedDataTopic;


	@Autowired ConsumerCreator consumerCreator;
	@Autowired FormDataRelayService formDataRelayService;
	@Autowired FormMediaProcessingService imageSubmittedAndReceivedDataProcessingService;
	@Autowired ISendToQueueDataService sendToQueueDataService;
	@Autowired MapConfigDataCrudService mapConfigDataCrudService;
	@Autowired
	TileProviderUtil TileProviderUtil;
	
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
		this.
		getAndProcessRecords(); 
	}

	public void getAndProcessRecords() {
		Consumer<String, String> consumer = consumerCreator.createConsumer(bootstrapAddress, groupIdConfig,  "5", Arrays.asList(bboxDataTopic));
		List<String> records;
		long startTs = System.currentTimeMillis();
		logger.info("In BboxConsumerService ::getAndProcessRecords()- Start Poll");
		while (true) {
			ConsumerRecords<String, String> consumerRecords = consumer.poll(Long.MAX_VALUE);
			if(consumerRecords == null) {
				logger.info("In BboxConsumerService ::getAndProcessRecords() - No record found");
				break;
			}

			if(consumerRecords.isEmpty()) {
				logger.debug("In BboxConsumerService ::getAndProcessRecords() -No record found");
				continue;
			}

			try {
				records = new ArrayList<>();
				for (ConsumerRecord<String, String> record: consumerRecords) {
					System.out.println("partition :: " +record.partition());
					System.out.println("offset :: " + record.offset());
					System.out.println("topic ::" +record.topic());
					records.add(record.value());
					if(record.topic().equals(bboxDataTopic)) {
						BboxContainerObject bboxDataObj = mapper.readValue(record.value().toString(),BboxContainerObject.class );
						if(bboxDataObj == null) {
							logger.info("In BboxConsumerService ::getAndProcessRecords() -  Submitted form data is null for "+ record);
							continue;
						}
						UUID superAppId = bboxDataObj.getSuperAppId();
						String mapConfigData = mapConfigDataCrudService.getMapConfiguration(superAppId, UUIDUtils.getDefaultUUID());
						String maxZoom = MapConstants.zmax;
						String minZoom = MapConstants.zmin;
						MapConfig config = null;
						if(mapConfigData != null && !mapConfigData.isEmpty()) {
							config = mapper.readValue(mapConfigData, MapConfig.class);
						} 
						if(config!= null && config.getMaxZoom() != null && config.getMaxZoom() > 0) {
							maxZoom = String.valueOf(config.getMaxZoom());
						}
						if(config!= null && config.getMinZoom() != null && config.getMinZoom() > 0) {
							minZoom = String.valueOf(config.getMinZoom());
						}
						String mapBaseUrl = null;
						if(config!= null) {
							mapBaseUrl = config.getMapBaseUrl();
						}
						TileProviderUtil.getTilesForABbox(bboxDataObj, maxZoom, minZoom, mapBaseUrl, String.valueOf(record.partition())+"_"+String.valueOf(record.offset()));
					}
				}

				logger.debug("In BboxConsumerService ::getAndProcessRecords() = Time taken for processing "+ records.size()+ " is " + (System.currentTimeMillis() - startTs));

			} catch (Exception e) {
				logger.debug("In BboxConsumerService ::getAndProcessRecords() - Exception is = " + e.getCause());
				e.printStackTrace();
			}
			logger.info("In BboxConsumerService ::getAndProcessRecords()- Commit start" );
			consumer.commitAsync();
			logger.info("In BboxConsumerService ::getAndProcessRecords()- Commit done" );
		}

		consumer.close();

	}

}
