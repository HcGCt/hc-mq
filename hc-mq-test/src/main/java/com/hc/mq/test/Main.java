package com.hc.mq.test;


import com.hc.mq.client.config.MqClientInitializer;
import com.hc.mq.client.consumer.IConsumer;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author hc
 */
public class Main {

    public static void main(String[] args) throws Exception {
        List<IConsumer> consumerList = Arrays.asList(new DemoConsumer1(), new DemoConsumer2());

        MqClientInitializer.getInstance().setConsumers(consumerList);
        MqClientInitializer.getInstance().start();

        while (!Thread.currentThread().isInterrupted()) {
            TimeUnit.MINUTES.sleep(2);
        }
        MqClientInitializer.getInstance().stop();
    }
}
