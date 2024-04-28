package com.hc.mq.server.core.disk;

import com.hc.mq.client.common.MqException;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;

import java.io.IOException;
import java.util.List;

/**
 * @Author hc
 */
public interface IMessageStore {

    // 创建队列文件
    void createQueueFiles(String queueName) throws IOException;

    // 存储消息
    void storeMessage(MessageQueue queue, Message message) throws IOException, MqException;

    // 删除消息
    void deleteMessage(MessageQueue queue, Message message) throws IOException;

    // 是否需要清理消息
    boolean isNeedClean(String queueName);

    // 清理消息
    void clean(MessageQueue queue) throws IOException, MqException;

    // 从队列中加载消息
    List<Message> loadAllValidMessage(String queueName) throws IOException, MqException;

    // 获取队列的存储目录
    String getQueueDir(String queueName);

    // 获取该队列的消息存储数据文件
    String getQueueDataPath(String queueName);

    // 获取该队列的消息存储统计文件
    String getQueueStatPath(String queueName);
}
