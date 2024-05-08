package com.hc.mq.common.remoting.service;

import com.hc.mq.common.comm.PullResult;
import com.hc.mq.common.comm.SendResult;
import com.hc.mq.common.message.Message;

import java.util.List;

/**
 * @Author hc
 */
public interface IMqService {


    /**
     * 批量新增消息
     *
     * @param messages
     * @return
     */
    SendResult sendMessages(List<Message> messages);

    SendResult sendHalfMessages(List<Message> messages, String transactionId);

    void commitOrRollback(String transactionId, byte rollbackOrCommit);
    /**
     * 拉取消息
     */
    PullResult pullMessage(String topic, String group, int consumerCount) throws Exception;


    int replicateToStore(List<Message> messages);

    void replicateToDelete(String msgId);
}
