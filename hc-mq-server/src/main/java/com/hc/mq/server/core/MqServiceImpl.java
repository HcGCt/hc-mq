package com.hc.mq.server.core;

import com.hc.mq.client.client.IMqService;
import com.hc.mq.client.common.MessageTransformState;
import com.hc.mq.client.common.PullResult;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;
import com.hc.mq.client.util.BinaryUtil;
import com.hc.mq.client.util.UniqueIdGenerator;
import com.hc.mq.server.core.disk.DefaultMessageStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hc.mq.client.common.Constants.*;
import static com.hc.mq.server.core.memo.MemoData.*;

/**
 * @Author hc
 */
public class MqServiceImpl implements IMqService {

    private static Logger logger = LoggerFactory.getLogger(MqServiceImpl.class);

    private DefaultMessageStore messageStore;

    public MqServiceImpl() {
        messageStore = DefaultMessageStore.getInstance();
    }

    @Override
    public SendResult sendMessages(List<Message> messages) {
        int count = 0;
        for (Message message : messages) {
            String topic = message.getTopic();
            MessageQueue messageQueue = topicMqMap.computeIfAbsent(topic, k -> {
                int queueId = UniqueIdGenerator.generateId();
                return new MessageQueue(queueId, topic, true);
            });
            if (messageQueue.isDurable()) {
                waitStoreMessages.add(message);
            }
            LinkedBlockingQueue<Message> messageLBQueue = queueMessageMap.computeIfAbsent(messageQueue.getQueueName(), k -> new LinkedBlockingQueue<>());
            messageLBQueue.add(message);
            // messageMap.put(message.getMsgId(), message);
            logger.info("已接收消息: {},添加至队列: {}", message.getMsgId(), messageQueue.getQueueName());
            if (messageQueue.isDurable())
                logger.debug("消息Id: {} 持久化消息...", message.getMsgId());
            ++count;
        }
        SendResult sendResult = new SendResult();
        sendResult.setState(MessageTransformState.SEND_ERROR);
        try {
            if (count > 0) {
                sendResult.setPayload(BinaryUtil.toByteArray(count));
                sendResult.setState(MessageTransformState.SEND_OK);
            }
        } catch (Exception e) {
            logger.error("设置SendResult错误");
        }

        return sendResult;
    }

    @Override
    public SendResult callbackSendMessages(List<Message> messages) {
        return null;
    }

    // ------------ send事务消息相关
    @Override
    public SendResult sendHalfMessages(List<Message> messages, String transactionId) {
        SendResult sendResult = new SendResult();
        try {
            LinkedBlockingQueue<Message> halfMessages = halfMessagesMap.computeIfAbsent(transactionId, k -> new LinkedBlockingQueue<>());
            halfMessages.addAll(messages);
            for (Message message : messages) {
                messageStore.storeMessage(transactionQueue, message);
            }
            sendResult.setTransactionId(transactionId);
            sendResult.setState(MessageTransformState.SEND_OK);
            sendResult.setQueueName(transactionQueue.getQueueName());
            logger.info("已接收事务消息消息, 事务Id: {},暂添加至队列: {}", transactionId, transactionQueue.getQueueName());
        } catch (Exception e) {
            logger.error("接受事务消息发送错误", e);
            sendResult.setState(MessageTransformState.SEND_ERROR);
        }

        return sendResult;
    }

    @Override
    public void commitOrRollback(String transactionId, String brokerName, byte rollbackOrCommit) {
        switch (rollbackOrCommit) {
            case COMMIT_MESSAGE: {
                System.out.println("test=======> 事务提交");
                // 提交事务
                LinkedBlockingQueue<Message> halfMessages = halfMessagesMap.get(transactionId);
                waitDeleteHalfMessages.addAll(halfMessages);
                while (!halfMessages.isEmpty()) {
                    try {
                        Message message = halfMessages.poll();
                        MessageQueue messageQueue = topicMqMap.computeIfAbsent(message.getTopic(), k -> {
                            int queueId = UniqueIdGenerator.generateId();
                            return new MessageQueue(queueId, message.getTopic(), true);
                        });
                        LinkedBlockingQueue<Message> messageLBQueue = queueMessageMap.computeIfAbsent(messageQueue.getQueueName(), k -> new LinkedBlockingQueue<>());
                        messageLBQueue.add(message);
                        logger.info("已接收消息: {},添加至队列: {}", message.getMsgId(), messageQueue.getQueueName());
                        if (messageQueue.isDurable()) {
                            messageStore.storeMessage(messageQueue, message);
                        }
                    } catch (Exception e) {
                        logger.error("提交事务消息失败：{}", e.getMessage());
                    }
                }
                halfMessagesMap.remove(transactionId);
            }
            break;
            case ROLLBACK_MESSAGE: {
                System.out.println("test=======> 事务回滚");
                // 回滚事务
                LinkedBlockingQueue<Message> halfMessages = halfMessagesMap.get(transactionId);
                waitDeleteHalfMessages.addAll(halfMessages);
                if (halfMessages != null && !halfMessages.isEmpty()) {
                    halfMessages.clear();
                    halfMessagesMap.remove(transactionId);
                }
            }
            break;
            case UNCERTAIN_TRAN: {
                // 回查 todo
            }
            break;
            default:
                break;
        }
    }

    @Override
    public PullResult pullMessage(String topic, String group, int consumerCount) throws Exception {
        PullResult pullResult = new PullResult();
        try {
            MessageQueue messageQueue = topicMqMap.get(topic);
            LinkedBlockingQueue<Message> messages = queueMessageMap.get(messageQueue.getQueueName());
            Message message = messages.take();
            // messageMap.remove(message.getMsgId());
            if (messageQueue.isDurable() && message.isStored()) {
                waitDeleteMessages.add(message);
            }
            pullResult.setMessage(message);
            pullResult.setState(MessageTransformState.SEND_OK);
            logger.info("消息pull, topic: {}, group: {}, queue: {}, msgId:{}", topic, group, messageQueue.getQueueName(), message.getMsgId());
        } catch (Exception e) {
            pullResult.setState(MessageTransformState.SEND_ERROR);
        }
        return pullResult;
    }


}
