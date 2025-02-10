//package org.alex.rocketmq;
//
//import org.apache.rocketmq.spring.core.RocketMQTemplate;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//
//@Component
//public class SpringProducer {
//
//    @Resource
//    private RocketMQTemplate rocketMQTemplate;
//
//    public void sendMessage(String topic, String message) {
//        this.rocketMQTemplate.convertAndSend(topic,message);
//    }
//
//    public void sendMessageInTransaction(String topic, String message) {
//
//        // this.rocketMQTemplate.sendMessageInTransaction(topic, message, null);
//    }
//}
