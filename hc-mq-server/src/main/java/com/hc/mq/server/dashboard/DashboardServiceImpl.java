package com.hc.mq.server.dashboard;

import com.hc.mq.common.message.Message;
import com.hc.mq.common.message.MessageQueue;
import com.hc.mq.common.remoting.service.DashboardService;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import static com.hc.mq.server.core.memo.MemoData.*;

/**
 * @Author hc
 */
public class DashboardServiceImpl implements DashboardService {

    @Override
    public List<Message> listMessagesByTopic(String topic) {
        List<Message> ret = new ArrayList<>();
        MessageQueue messageQueue = topicMqMap.get(topic);
        if (messageQueue == null) return ret;
        String queueName = messageQueue.getQueueName();
        LinkedBlockingQueue<Message> messages = queueMessageMap.get(queueName);
        if (messages != null) {
            ret.addAll(messages);
        }
        return ret;
    }

    @Override
    public List<String> listTopics() {
        List<String> ret = new ArrayList<>();
        Set<String> topics = topicMqMap.keySet();
        if (topics != null || topics.size() > 0) ret.addAll(topics);
        return ret;
    }

    @Override
    public List<MessageQueue> listQueuesByTopic(String topic) {
        List<MessageQueue> ret = new ArrayList<>();
        MessageQueue messageQueue = topicMqMap.get(topic);
        if (messageQueue != null) {
            ret.add(messageQueue);
        }
        return ret;
    }
}
