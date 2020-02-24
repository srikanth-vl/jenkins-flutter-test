package com.vassarlabs.prod.kafka.common;

import java.util.List;
import java.util.Properties;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@ComponentScan
@Configuration
@PropertySource("classpath:kafka.properties")
public class ConsumerCreator {
	
	@Value(value = "${kafka_brokers}")
	private String bootstrapAddress;
	
	@Value(value = "${group_id_config}")
	private String group_id_config;
	
	@Value(value = "${max_poll_records}")
	private String max_poll_records;
	
	@Value(value = "${offset_reset_earliest}")
	private String offset_reset_earliest;
	
	@Value("${test_topic_name}")
	private String topic_name;
	
	 public  Consumer<String, String> createConsumer(String kafkaServers, String comsumerId, String maxRecords, List<String> topic) {
	        final Properties props = new Properties();
	        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServers);
	        props.put(ConsumerConfig.GROUP_ID_CONFIG, comsumerId);
	        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class.getName());
	        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,StringDeserializer.class.getName());
	        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxRecords);
	        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
	        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, offset_reset_earliest);
	        props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 10485760);
	        // Create the consumer using props.
	        final Consumer<String, String> consumer = new KafkaConsumer<>(props);
	        // Subscribe to the topic.
	        consumer.subscribe(topic);
	        return consumer;
	    }
	 

}