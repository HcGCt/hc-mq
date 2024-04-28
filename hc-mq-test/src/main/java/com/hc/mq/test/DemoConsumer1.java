package com.hc.mq.test;

import com.hc.mq.client.consumer.IConsumer;
import com.hc.mq.client.consumer.annotation.Consumer;
import com.hc.mq.client.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hc
 */
@Consumer(topic = "testTopic1")
public class DemoConsumer1 implements IConsumer {

    private Logger logger = LoggerFactory.getLogger(DemoConsumer1.class);

    @Override
    public boolean consume(Message message) {
        logger.info("消费消息成功! 消息id: {}", message.getMsgId());
        return true;
    }
}
