package com.hc.mq.client.consumer;


import com.hc.mq.common.message.Message;

/**
 * @Author hc
 */
public interface IConsumer {

    // boolean subscribe(String topic, boolean autoAck, ConsumerHandler consumerHandler);

    boolean consume(Message message);
}
