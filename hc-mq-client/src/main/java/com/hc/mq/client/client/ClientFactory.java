package com.hc.mq.client.client;

import com.hc.mq.client.common.MqException;
import com.hc.mq.client.common.PullResult;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.consumer.IConsumer;
import com.hc.mq.client.consumer.thread.PullMessageThread;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.producer.SendCallback;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.invoker.CallType;
import com.hc.rpc.invoker.RpcInvokeCallback;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;
import com.hc.rpc.protocol.serialize.HessianSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 客户端工厂
 *
 * @Author hc
 */
public class ClientFactory {
    private final static Logger logger = LoggerFactory.getLogger(ClientFactory.class);

    private String serverAddress;                       // 若确定serverAddress则不负载均衡

    // rpc 服务
    private static IMqService synClient;                   // 远程服务接口
    private static IMqService callbackClient;                   // 远程服务接口
    private RpcInvokerFactory rpcInvokerFactory;
    private static RpcReferenceBean referenceBean;

    // 线程池
    private ExecutorService clientThreadPool = Executors.newCachedThreadPool();
    public static volatile boolean clientThreadPoolStopFlag = false;


    // 启动服务
    public void start() throws Exception {
        rpcInvokerFactory = RpcInvokerFactory.getInstance();
        try {
            RpcReferenceBean referenceBean = RpcInvokerFactory.createRpcReferenceBean(3000);
            referenceBean.setVersion(RpcConfig.getInstance().getVersion());
            referenceBean.setServerAddress(serverAddress);
            synClient = referenceBean.getObject(IMqService.class);

            referenceBean.setCallType(CallType.CALLBACK);
            callbackClient = referenceBean.getObject(IMqService.class);

        } catch (Exception e) {
            logger.error("获取IClient失败：{}", e.getMessage());
            throw new RuntimeException(e);
        }

        // 异步发送消息线程池
        startSendMessageASYN();

        // 消费者线程池
        startConsumerThread();

    }

    public void stop() throws Exception {
        // 清除消费者
        stopConsumerThread();
        // 停止线程池
        clientThreadPoolStopFlag = true;
        clientThreadPool.shutdown();
        rpcInvokerFactory.stop();
    }

    // ------------- 消费者:拉模式消费消息 -------------
    private List<IConsumer> consumerList;
    private List<PullMessageThread> consumerThreads = new ArrayList<>();

    private void startConsumerThread() {
        if (consumerList == null || consumerList.isEmpty()) {
            logger.warn("没有消费者!");
            return;
        }

        // 添加 消费者线程
        for (IConsumer consumer : consumerList) {
            // 验证消费者
            consumerThreads.add(new PullMessageThread(consumer));
        }
        if (consumerThreads.isEmpty()) {
            logger.warn("没有消费者!");
            return;
        }

        for (PullMessageThread thread : consumerThreads) {
            clientThreadPool.execute(thread);
            logger.info("消费者线程启动成功! topic:{}, group:{}",
                    thread.getConsumerAnnotation().topic(), thread.getConsumerAnnotation().group());
        }
    }


    private void stopConsumerThread() {
        if (consumerThreads.isEmpty()) {
            return;
        }
        consumerThreads.clear();
    }

    // 暂存消息,用于异步发送
    private static LinkedBlockingQueue<Message> tempMessageQueue = new LinkedBlockingQueue<>();

    // --------------- 生产者接口 ---------------

    /**
     * 同步发送消息
     */
    public static void sendMessagesSYN(List<Message> messages) {
        synClient.sendMessages(messages);
    }

    /**
     * 异步发送消息
     */
    public static void sendMessagesASYN(List<Message> messages) {
        tempMessageQueue.addAll(messages);
    }

    private void startSendMessageASYN() {
        // 2个线程
        for (int i = 0; i < 2; i++) {
            clientThreadPool.execute(() -> {
                while (!clientThreadPoolStopFlag) {
                    try {
                        Message message = tempMessageQueue.take();
                        // 批量发送
                        List<Message> messages = new ArrayList<>();
                        messages.add(message);
                        List<Message> otherMessageList = new ArrayList<>();
                        int drain = tempMessageQueue.drainTo(otherMessageList, 100);
                        if (drain > 0) {
                            messages.addAll(otherMessageList);
                        }
                        synClient.sendMessages(messages);
                    } catch (Exception e) {
                        if (!clientThreadPoolStopFlag) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            });
        }
    }

    // todo bug 回调响应阻塞？
    public static void sendMessagesCallback(List<Message> messages, final SendCallback<SendResult> callback) {
        RpcInvokeCallback.setCallback(new RpcInvokeCallback<SendResult>() {
            @Override
            public void onSuccess(SendResult result) {
                callback.onSuccess(result);
            }

            @Override
            public void onFailure(Throwable throwable) {
                callback.onException(throwable);
            }
        });
        callbackClient.sendMessages(messages);
    }


    public static SendResult sendHalfMessages(List<Message> messages, String transactionId) {
        return synClient.sendHalfMessages(messages, transactionId);
    }

    public static void commitOrRollback(String transactionId, String brokerName, byte rollbackOrCommit) {
        synClient.commitOrRollback(transactionId, brokerName, rollbackOrCommit);
    }


    // --------------- 消费者接口 ---------------
    // todo,异步拉取?
    public static Message pullMessage(String topic, String group, int consumerCount) throws Exception {
        PullResult pullResult = synClient.pullMessage(topic, group, consumerCount);
        Message message = pullResult.getMessage();
        return message;
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public List<IConsumer> getConsumerList() {
        return consumerList;
    }

    public void setConsumerList(List<IConsumer> consumerList) {
        this.consumerList = consumerList;
    }
}
