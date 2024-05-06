package com.hc.mq.server.core.memo;


import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;
import com.hc.mq.server.util.ConcurrentHashSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hc.mq.client.common.Constants.TRANSACTION_TOPIC;

/**
 * @Author hc
 */
public class MemoData {
    // key 队列名，value 队列对象
    public static Map<String, MessageQueue> queueMap = new ConcurrentHashMap<>();
    // key 主题，第二个 key 队列名
    public static Map<String, MessageQueue> topicMqMap = new ConcurrentHashMap<>();
    // 存储已有的消息id
    public static Set<String> messageIdSet = new ConcurrentHashSet<>();
    // key 队列名，value 队列中的消息集合LinkedBlockingQueue表示
    public static Map<String, LinkedBlockingQueue<Message>> queueMessageMap = new ConcurrentHashMap<>();
    // 第一个 key 队列名，第二个 key 唯一消息Id
    public static Map<String, Map<String, Message>> queueMessageWaitAckMap = new ConcurrentHashMap<>();

    // 待持久化的消息
    public static LinkedBlockingQueue<Message> waitStoreMessages = new LinkedBlockingQueue<>();

    // 待删除的消息
    public static LinkedBlockingQueue<Message> waitDeleteMessages = new LinkedBlockingQueue<>();


    // 事务半消息    key 事务Id，value 半消息队列
    public static Map<String, LinkedBlockingQueue<Message>> halfMessagesMap = new ConcurrentHashMap<>();
    public static LinkedBlockingQueue<Message> waitDeleteHalfMessages = new LinkedBlockingQueue<>();

    public static MessageQueue transactionQueue = new MessageQueue(0, TRANSACTION_TOPIC);

    // 集群异步复制的消息
    public static LinkedBlockingQueue<Message> waitStoreReplicateMessages = new LinkedBlockingQueue<>();
    public static LinkedBlockingQueue<Message> waitDeleteReplicateMessages = new LinkedBlockingQueue<>();
    public static Set<String> waitDeleteMessageIdSet = new ConcurrentHashSet<>();
}
