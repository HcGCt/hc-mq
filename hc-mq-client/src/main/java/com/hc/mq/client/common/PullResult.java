package com.hc.mq.client.common;


import com.hc.mq.client.message.Message;

import java.io.Serializable;

/**
 * @Author hc
 */
public class PullResult implements Serializable {
    private MessageTransformState state;

    private Message message;

    public PullResult() {

    }

    public PullResult(MessageTransformState state, Message message) {
        this.state = state;
        this.message = message;
    }

    public MessageTransformState getState() {
        return state;
    }

    public void setState(MessageTransformState state) {
        this.state = state;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }
}
