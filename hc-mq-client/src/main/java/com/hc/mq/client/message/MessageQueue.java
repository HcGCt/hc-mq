package com.hc.mq.client.message;

import java.io.Serializable;

import static com.hc.mq.client.common.Constants.DEFAULT_BROKER;

/**
 * 消息队列信息
 *
 * @Author hc
 */
public class MessageQueue implements Comparable<MessageQueue>, Serializable {
    // 队列Id
    protected int queueId;
    // 主题
    protected String topic;
    // todo
    protected String brokerName;
    // 持久化存储
    protected boolean durable = false;
    // 无消费者自动删除队列
    protected boolean autoDelete = false;

    // 队列名：topic#queueId
    protected String queueName;

    public MessageQueue() {

    }

    public MessageQueue(int queueId, String topic, String brokerName, boolean durable, boolean autoDelete, String queueName) {
        this.queueId = queueId;
        this.topic = topic;
        this.brokerName = brokerName;
        this.durable = durable;
        this.autoDelete = autoDelete;
        this.queueName = queueName;
    }

    public MessageQueue(int queueId, String topic) {
        this(queueId, topic, DEFAULT_BROKER, false, false, topic + "#" + queueId);
    }

    public MessageQueue(int queueId, String topic, boolean durable) {
        this(queueId, topic, DEFAULT_BROKER, durable, false, topic + "#" + queueId);
    }


    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    @Override
    public int compareTo(MessageQueue q) {
        // 排序顺序：topic、queueId
        int topicCompareTo = this.topic.compareTo(q.topic);
        if (topicCompareTo != 0) {
            return topicCompareTo;
        }

        return this.queueId - q.queueId;
    }
}
