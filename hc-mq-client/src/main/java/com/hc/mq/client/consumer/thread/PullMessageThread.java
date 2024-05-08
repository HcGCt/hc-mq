package com.hc.mq.client.consumer.thread;

import com.hc.mq.client.client.ClientFactory;
import com.hc.mq.client.consumer.IConsumer;
import com.hc.mq.client.consumer.annotation.Consumer;
import com.hc.mq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hc.mq.client.client.ClientFactory.clientThreadPoolStopFlag;

/**
 * 每个消费者拥有一个线程 pull消息
 *
 * @Author hc
 */
public class PullMessageThread extends Thread {

    private final static Logger logger = LoggerFactory.getLogger(PullMessageThread.class);

    private IConsumer consumer;      // 消费者实体
    private Consumer consumerAnnotation;    // 消费者标识,注解

    public PullMessageThread(IConsumer consumer) {
        this.consumer = consumer;
        this.consumerAnnotation = consumer.getClass().getAnnotation(Consumer.class);
    }

    @Override
    public void run() {
        while (!clientThreadPoolStopFlag) {
            try {
                String topic = consumerAnnotation.topic();
                String group = consumerAnnotation.group();
                // todo 暂不考虑消费组
                Message message = ClientFactory.pullMessage(topic, group, 1);
                boolean consume = consumer.consume(message);
                logger.info("消费消息成功: {}", consume);
            } catch (Exception e) {

            }
        }
    }


    public IConsumer getConsumer() {
        return consumer;
    }

    public void setConsumer(IConsumer consumer) {
        this.consumer = consumer;
    }

    public Consumer getConsumerAnnotation() {
        return consumerAnnotation;
    }

    public void setConsumerAnnotation(Consumer consumerAnnotation) {
        this.consumerAnnotation = consumerAnnotation;
    }
}
