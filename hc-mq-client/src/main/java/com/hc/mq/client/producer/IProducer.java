package com.hc.mq.client.producer;


import com.hc.mq.common.message.Message;

import java.util.List;

/**
 * @Author hc
 */
public interface IProducer {
    // 批量发送
    void send(List<Message> messages, boolean syn);

    int sendCallback(List<Message> messages, SendCallback sendCallback);

    // 批量广播
    void broadcast(List<Message> message, boolean syn);
}
