//package com.vassarlabs.prod.kafka.common;
//
//import java.util.Properties;
//
//import org.apache.kafka.clients.producer.KafkaProducer;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.apache.kafka.common.serialization.LongSerializer;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.PropertySource;
//import org.springframework.kafka.support.serializer.JsonSerializer;
//@ComponentScan
//@Configuration
//@PropertySource("classpath:kafka.properties")
//public class ProducerCreator {
//	@Value(value = "${kafka_brokers}")
//	private   String bootstrapAddress;
//	
//	@Value("${client_id}")
//	private  String client_id;
//	
//    public  Producer<Long, Object> createProducer() {
//
//        Properties props = new Properties();
//
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapAddress );
//
//        props.put(ProducerConfig.CLIENT_ID_CONFIG, client_id);
//
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class.getName());
//
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,JsonSerializer.class);
//        //props.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, CustomPartitioner.class.getName());
//
//        return new KafkaProducer<>(props);
//
//    }
//
//}