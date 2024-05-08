package com.hc.mq.common.message;


import com.hc.rpc.utils.UUIDUtils;

import java.io.Serializable;

/**
 * @Author hc
 */
public class Message implements Serializable {
    // todo 唯一有序Id
    private String msgId;
    // 消息主题
    private String topic;
    // 消息体
    private byte[] body;
    // 合法性
    private byte valid = 0x1;
    // 创建时间戳
    private long createTimestamp;
    // 文件存储偏移量
    private transient long offsetBegin = 0;
    private transient long offsetEnd = 0;

    private boolean stored = false;

    private String transactionId;

    public Message() {

    }

    public Message(String msgId, String topic, byte[] body, byte valid, long createTimestamp, long offsetBegin, long offsetEnd) {
        this.msgId = msgId;
        this.topic = topic;
        this.body = body;
        this.valid = valid;
        this.createTimestamp = createTimestamp;
        this.offsetBegin = offsetBegin;
        this.offsetEnd = offsetEnd;
    }

    public Message(Message other) {
        this.msgId = other.msgId;
        this.topic = other.topic;
        this.body = other.body;
        this.valid = other.valid;
        this.createTimestamp = other.createTimestamp;
        this.offsetBegin = other.offsetBegin;
        this.offsetEnd = other.offsetEnd;
        this.transactionId = other.transactionId;
    }

    public Message(String topic, byte[] body) {
        this(UUIDUtils.createUUID16(), topic, body, (byte) 0x1, System.currentTimeMillis(), 0L, 0L);
    }

    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public int getValid() {
        return valid;
    }

    public void setValid(byte valid) {
        this.valid = valid;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }

    public void setCreateTimestamp(long createTimestamp) {
        this.createTimestamp = createTimestamp;
    }

    public long getOffsetBegin() {
        return offsetBegin;
    }

    public void setOffsetBegin(long offsetBegin) {
        this.offsetBegin = offsetBegin;
    }

    public long getOffsetEnd() {
        return offsetEnd;
    }

    public void setOffsetEnd(long offsetEnd) {
        this.offsetEnd = offsetEnd;
    }

    public boolean isStored() {
        return stored;
    }

    public void setStored(boolean stored) {
        this.stored = stored;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
}
