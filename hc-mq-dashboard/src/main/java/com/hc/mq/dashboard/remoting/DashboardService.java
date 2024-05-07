package com.hc.mq.dashboard.remoting;

import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;

import java.util.List;

/**
 * @Author hc
 */
public interface DashboardService {
    List<Message> listMessagesByTopic(String topic);

    List<String> listTopics();

    List<MessageQueue> listQueuesByTopic(String topic);
}
