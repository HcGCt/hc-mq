package com.hc.mq.client.common;


import com.hc.mq.client.message.Message;

import java.io.Serializable;

/**
 * @Author hc
 */
public class PullResult implements Serializable {
    private MessageTransformState state;

    private Message message;

    private String responseBroker;

    public PullResult() {

    }

    public PullResult(MessageTransformState state, Message message) {
        this.state = state;
        this.message = message;
    }

    public PullResult(MessageTransformState state, Message message, String responseBroker) {
        this.state = state;
        this.message = message;
        this.responseBroker = responseBroker;
    }

    public String getResponseBroker() {
        return responseBroker;
    }

    public void setResponseBroker(String responseBroker) {
        this.responseBroker = responseBroker;
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
