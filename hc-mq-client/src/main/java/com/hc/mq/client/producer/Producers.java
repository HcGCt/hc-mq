package com.hc.mq.client.producer;

import com.hc.mq.client.client.ClientFactory;
import com.hc.mq.client.common.LocalTransactionState;
import com.hc.mq.client.common.MessageTransformState;
import com.hc.mq.client.common.MqException;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.producer.transaction.TransactionListener;
import com.hc.rpc.utils.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

import static com.hc.mq.client.common.Constants.*;

/**
 * @Author hc
 */
public class Producers {

    private static Logger logger = LoggerFactory.getLogger(Producers.class);

    // 默认同步发送消息
    public static void send(Message message) {
        send(message, true);
    }

    public static void send(Message message, boolean syn) {
        send(Arrays.asList(message), syn);
    }

    // 批量发送
    public static void send(List<Message> messages, boolean syn) {
        validMessage(messages);
        if (syn) {
            // 同步发送
            ClientFactory.sendMessagesSYN(messages);
        } else {
            // 异步发送
            ClientFactory.sendMessagesASYN(messages);
        }
    }

    public static void sendCallback(Message message, SendCallback callback) {
        sendCallback(Arrays.asList(message), callback);
    }

    // callback发送
    public static void sendCallback(List<Message> messages, SendCallback callback) {
        validMessage(messages);
        ClientFactory.sendMessagesCallback(messages, callback);
    }


    // 广播消息 todo
    public static void broadcast(Message message, boolean syn) {
        broadcast(Arrays.asList(message), syn);
    }

    public static void broadcast(List<Message> message, boolean syn) {

    }


    // 事务相关
    public static SendResult sendMessagesInTransaction(List<Message> messages, final TransactionListener transactionListener, Object arg) {
        if (transactionListener == null) {
            throw new MqException("本地事务监听器为null");
        }
        String transactionId = UUIDUtils.createUUID32();
        // 发送半消息
        SendResult sendResult;
        try {
            sendResult = ClientFactory.sendHalfMessages(messages, transactionId);
        } catch (Exception e) {
            throw new MqException("发送事务消息失败");
        }
        MessageTransformState state = sendResult.getState();
        LocalTransactionState localTransactionState = LocalTransactionState.UNKNOW;
        switch (state) {
            case SEND_OK: {
                transactionId = sendResult.getTransactionId();
                // 执行本地事务
                localTransactionState = transactionListener.executeLocalTransaction(messages, arg);
                if (localTransactionState != LocalTransactionState.COMMIT_MESSAGE) {
                    // 本地事务执行失败
                }
            }
            break;
            case SEND_ERROR:
                localTransactionState = LocalTransactionState.ROLLBACK_MESSAGE; // 回滚
                break;
            default:
                break;
        }

        // 提交或回滚事务
        try {
            commitOrRollback(messages, sendResult, localTransactionState);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        SendResult result = new SendResult();
        result.setQueueName(sendResult.getQueueName());
        result.setState(sendResult.getState());
        result.setTransactionId(sendResult.getTransactionId());
        result.setPayload(result.getPayload());
        result.setLocalTransactionState(localTransactionState);
        return result;
    }

    private static void commitOrRollback(List<Message> messages, SendResult sendResult, LocalTransactionState localTransactionState) {
        String transactionId = sendResult.getTransactionId();
        byte rc = UNCERTAIN_TRAN;
        switch (localTransactionState) {
            case COMMIT_MESSAGE:
                rc = COMMIT_MESSAGE;
                break;
            case ROLLBACK_MESSAGE:
                rc = ROLLBACK_MESSAGE;
                break;
            case UNKNOW:
                rc = UNCERTAIN_TRAN;
                break;
            default:
                break;
        }
        ClientFactory.commitOrRollback(transactionId, null, rc);
    }



    /**
     * 验证消息合法性
     */
    private static void validMessage(Message message) {
        if (message == null) {
            throw new MqException("发送消息失败：消息不能为空!");
        }
        if (message.getTopic().isEmpty()) {
            throw new MqException("发送消息失败：未指定消息主题!");
        }

        if (message.getCreateTimestamp() == 0) {
            message.setCreateTimestamp(System.currentTimeMillis());
        }

        // 其他 todo
    }

    private static void validMessage(List<Message> messages) {
        for (Message message : messages) {
            validMessage(message);
        }
    }
}
