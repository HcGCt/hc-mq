package com.hc.mq.server.core;

import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;
import com.hc.mq.server.core.config.MqServerConfig;
import com.hc.mq.server.core.disk.DefaultMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.hc.mq.server.core.memo.MemoData.*;

/**
 * @Author hc
 */
public class Broker {
    private static Logger logger = LoggerFactory.getLogger(Broker.class);

    private String ip;
    private int port;
    // ------------ 持久化 -------------
    private DefaultMessageStore messageStore;

    private Broker() {
        ip = MqServerConfig.getInstance().getIp();
        port = MqServerConfig.getInstance().getPort();
        messageStore = DefaultMessageStore.getInstance();
    }

    private static Broker instance = new Broker();

    public static Broker init() {
        return instance;
    }


    // ------------- 服务器开关 -------------

    public Broker start() throws Exception {
        Server server = Server.getInstance();
        server.init(ip, port).start();
        startThreadPool();
        return instance;
    }

    public void stop() throws Exception {
        Server server = Server.getInstance();
        server.stop();
        stopThreadPool();
    }


    // ------------- 服务端线程 ------------
    private ExecutorService executorService = Executors.newCachedThreadPool();
    private volatile boolean executorStopped = false;

    public void startThreadPool() {
        // 1.向消费者推消息,线程个数:2
        for (int i = 0; i < 2; i++) {
            executorService.execute(() -> {
                while (!executorStopped) {

                }
            });
        }

        // 2.异步接收生产者持久化消息,线程个数:2
        for (int i = 0; i < 2; i++) {
            executorService.execute(() -> {
                while (!executorStopped) {
                    try {
                        // todo 批量写入磁盘
                        Message message = waitStoreMessages.take();
                        if (!message.isStored()) {
                            String topic = message.getTopic();
                            MessageQueue queue = topicMqMap.get(topic);
                            messageStore.storeMessage(queue, message);
                            message.setStored(true);
                        }
                    } catch (Exception e) {
                        if (!executorStopped) {
                            logger.error("线程池发生错误: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        // 3.删除磁盘上的消息
        for (int i = 0; i < 2; i++) {
            executorService.execute(() -> {
                while (!executorStopped) {
                    try {
                        // todo 批量删除
                        Message message = waitDeleteMessages.take();
                        if (message.isStored()) {
                            String topic = message.getTopic();
                            MessageQueue queue = topicMqMap.get(topic);
                            messageStore.deleteMessage(queue, message);
                            message.setStored(false);
                        }
                    } catch (Exception e) {
                        if (!executorStopped) {
                            logger.error("线程池发生错误: {}", e.getMessage());
                            throw new RuntimeException(e);
                        }
                    }
                }
            });
        }

        // 4.删除事务队列中临时事务消息
        executorService.execute(() -> {
            while (!executorStopped) {
                try {
                    Message message = waitDeleteHalfMessages.take();
                    messageStore.deleteMessage(transactionQueue, message);
                } catch (Exception e) {
                    if (!executorStopped) {
                        logger.error("线程池发生错误: {}", e.getMessage());
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    public void stopThreadPool() {
        if (!executorStopped) {
            synchronized (this) {
                executorService.shutdown();
            }
        }
    }

    // ------------- 生产者 -------------


    // ------------- 消费者 -------------

}
