//package com.vassarlabs.prod.kafka.producer;
//
//import org.apache.kafka.clients.producer.ProducerConfig;
//import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.integration.annotation.Gateway;
//import org.springframework.integration.annotation.MessagingGateway;
//import org.springframework.integration.annotation.ServiceActivator;
//import org.springframework.integration.kafka.outbound.KafkaProducerMessageHandler;
//import org.springframework.kafka.core.DefaultKafkaProducerFactory;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.kafka.core.ProducerFactory;
//import org.springframework.kafka.support.KafkaHeaders;
//import org.springframework.messaging.MessageHandler;
//import org.springframework.messaging.handler.annotation.Header;
//
//import java.util.Map;
//
//@Configuration
//public class ProducerConfiguration {
//
////    @Autowired
////    private KafkaAppProperties properties;
//
//    @Bean
//    public ProducerFactory<?, ?> kafkaProducerFactory(KafkaProperties properties) {
//        Map<String, Object> producerProperties = properties.buildProducerProperties();
//        producerProperties.put(ProducerConfig.LINGER_MS_CONFIG, 5);
//        producerProperties.put(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, 104857600);
//        producerProperties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4");
//        producerProperties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 60000);
//        return new DefaultKafkaProducerFactory<>(producerProperties);
//    }
//
//
//    @ServiceActivator(inputChannel = "toKafka")
//    @Bean
//    public MessageHandler handler(KafkaTemplate<String, String> kafkaTemplate) {
//        KafkaProducerMessageHandler<String, String> handler =
//                new KafkaProducerMessageHandler<>(kafkaTemplate);
//        //handler.setMessageKeyExpression(new LiteralExpression(this.properties.getMessageKey()));
//        //handler.setSync(true);
//        return handler;
//    }
//
//
//    @MessagingGateway
//    public interface KafkaGateway {
//
//        @Gateway(requestChannel = "toKafka")
//        void sendToKafka(Object payload, @Header(KafkaHeaders.TOPIC) String topic);
//    }
//
//
//}
