package com.hc.mq.client.common;

import java.io.Serializable;

/**
 * @Author hc
 */
public class SendResult implements Serializable {
    private MessageTransformState state;

    private String queueName;

    private String transactionId = "-1";  // 非事务则transactionId为-1

    private LocalTransactionState localTransactionState;

    private byte[] payload; // 响应负载

    public SendResult() {

    }

    public SendResult(MessageTransformState state, String queueName) {
        this.state = state;
        this.queueName = queueName;
    }

    public SendResult(MessageTransformState state, String queueName, byte[] payload) {
        this.state = state;
        this.queueName = queueName;
        this.payload = payload;
    }

    public SendResult(MessageTransformState state, String queueName, String transactionId, byte[] payload) {
        this.state = state;
        this.queueName = queueName;
        this.transactionId = transactionId;
        this.payload = payload;
    }

    public MessageTransformState getState() {
        return state;
    }

    public void setState(MessageTransformState state) {
        this.state = state;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }

    public LocalTransactionState getLocalTransactionState() {
        return localTransactionState;
    }

    public void setLocalTransactionState(LocalTransactionState localTransactionState) {
        this.localTransactionState = localTransactionState;
    }
}
