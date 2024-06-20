package org.alex.rocketmq;

import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Component
@RocketMQMessageListener(consumerGroup = "alexAiConsumer" ,topic = "tesatTopic",consumeMode = ConsumeMode.ORDERLY)
public class SpringConsumer implements RocketMQListener<String> {
    @Override
    public void onMessage(String s) {
        System.out.println("recieve: "+s);
    }
}
