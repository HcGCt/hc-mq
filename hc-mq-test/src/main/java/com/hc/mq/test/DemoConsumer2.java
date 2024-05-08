package com.hc.mq.test;

import com.hc.mq.client.consumer.IConsumer;
import com.hc.mq.client.consumer.annotation.Consumer;
// import com.hc.mq.client.message.Message;
import com.hc.mq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hc
 */
@Consumer(topic = "testTopic2")
public class DemoConsumer2 implements IConsumer {
    private Logger logger = LoggerFactory.getLogger(DemoConsumer2.class);

    @Override
    public boolean consume(Message message) {
        logger.info("消费消息成功! 消息id: {}", message.getMsgId());
        return true;
    }
}
