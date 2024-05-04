package com.hc.mq.client.config;

import com.hc.mq.client.client.ClientFactory;
import com.hc.mq.client.consumer.IConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @Author hc
 */
public class MqClientInitializer {
    private static Logger logger = LoggerFactory.getLogger(MqClientInitializer.class);

    private static volatile MqClientInitializer instance;

    private MqClientInitializer() {
    }

    public static MqClientInitializer getInstance() {
        if (instance == null) {
            synchronized (MqClientInitializer.class) {
                if (instance == null) {
                    instance = new MqClientInitializer();
                }
            }
        }
        return instance;
    }

    /**
     * 客户端相关
     */
    private ClientFactory clientFactory;

    // 启动
    public void start() throws Exception {
        if (clientFactory == null) {
            init();
        }
        clientFactory.start();
    }

    // 停止
    public void stop() throws Exception {
        if (clientFactory == null) return;
        clientFactory.stop();
    }

    // 设置消费者
    public void setConsumers(List<IConsumer> consumerList) {
        if (clientFactory == null) {
            init();
        }
        clientFactory.setConsumerList(consumerList);
    }

    private void init() {
        if (clientFactory == null) {
            clientFactory = new ClientFactory();
            // clientFactory.setServerAddress(MqClientConfig.getInstance().getServerAddress());
        }
    }
}
