//package com.vassarlabs.prod.kafka.producer;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Component;
//@Component
//public class MessageProducer {
//
//    @Autowired
//    private KafkaTemplate<String, String> kafkaTemplate;
//
//    @Autowired
////    private KafkaTemplate<String, Greeting> greetingKafkaTemplate;
//
//    @Value(value = "${message.topic.name}")
//    private String topicName;
//
////    @Value(value = "${partitioned.topic.name}")
////    private String partionedTopicName;
////
////    @Value(value = "${filtered.topic.name}")
////    private String filteredTopicName;
////
////    @Value(value = "${greeting.topic.name}")
////    private String greetingTopicName;
//
//    public void sendMessage(String message) {
//        kafkaTemplate.send(topicName, message);
//    }
//
////    public void sendMessageToPartion(String message, int partition) {
////        kafkaTemplate.send(partionedTopicName, partition, message);
////    }
////
////    public void sendMessageToFiltered(String message) {
////        kafkaTemplate.send(filteredTopicName, message);
////    }
////
////    public void sendGreetingMessage(Greeting greeting) {
////        greetingKafkaTemplate.send(greetingTopicName, greeting);
////    }
//}