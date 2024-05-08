package com.hc.mq.common.remoting.service;

import com.hc.mq.common.message.Message;
import com.hc.mq.common.message.MessageQueue;

import java.util.List;

/**
 * @Author hc
 */
public interface DashboardService {
    List<Message> listMessagesByTopic(String topic);

    List<String> listTopics();

    List<MessageQueue> listQueuesByTopic(String topic);
}
