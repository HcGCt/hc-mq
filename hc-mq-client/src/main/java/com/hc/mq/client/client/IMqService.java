package com.hc.mq.client.client;

import com.hc.mq.client.common.PullResult;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.message.Message;

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


    /**
     * 异步批量新增消息
     *
     * @param messages
     * @return
     */
    SendResult callbackSendMessages(List<Message> messages);

    SendResult sendHalfMessages(List<Message> messages, String transactionId);

    void commitOrRollback(String transactionId, String brokerName, byte rollbackOrCommit);
    /**
     * 拉取消息
     */
    PullResult pullMessage(String topic, String group, int consumerCount) throws Exception;

}
