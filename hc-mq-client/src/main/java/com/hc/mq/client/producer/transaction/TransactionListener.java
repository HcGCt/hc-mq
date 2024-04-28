package com.hc.mq.client.producer.transaction;


import com.hc.mq.client.common.LocalTransactionState;
import com.hc.mq.client.message.Message;

import java.util.List;


/**
 * @Author hc
 */
public interface TransactionListener {
    /**
     * 半消息发送成功,执行本地事务
     */
    LocalTransactionState executeLocalTransaction(final List<Message> messages, final Object arg);

    /**
     * 半消息发送状态位置,检查本地事务
     */
    LocalTransactionState checkLocalTransaction(final Message msg);
}
