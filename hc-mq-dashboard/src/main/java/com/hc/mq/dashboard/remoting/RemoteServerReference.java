package com.hc.mq.dashboard.remoting;

import com.hc.mq.client.message.Message;
import com.hc.mq.client.message.MessageQueue;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author hc
 */
@Component
@Scope(value = "singleton")
public class RemoteServerReference {
    private static Logger logger = LoggerFactory.getLogger(RemoteServerReference.class);

    private final DashboardService dashboardService;

    public RemoteServerReference() {
        RpcReferenceBean rpcReferenceBean = RpcInvokerFactory.createRpcReferenceBean(3000000);  // todebug
        try {
            dashboardService = rpcReferenceBean.getObject(DashboardService.class);
        } catch (Exception e) {
            logger.error("获取远程服务错误:", e);
            throw new RuntimeException(e);
        }
    }

    public List<Message> listMessagesByTopic(String topic) {
        List<Message> messages;
        try {
            messages = dashboardService.listMessagesByTopic(topic);
        } catch (Exception e) {
            logger.error("获取broker 【消息】 失败: ", e);
            throw new RuntimeException(e);
        }
        return messages;
    }

    public List<String> listTopics() {
        List<String> topics;
        try {
            topics = dashboardService.listTopics();
        } catch (Exception e) {
            logger.error("获取broker 【主题】 失败", e);
            throw new RuntimeException(e);
        }
        return topics;
    }

    public List<MessageQueue> listQueuesByTopic(String topic) {
        List<MessageQueue> messageQueues;
        try {
            messageQueues = dashboardService.listQueuesByTopic(topic);
        } catch (Exception e) {
            logger.error("获取broker 【队列】 失败", e);
            throw new RuntimeException(e);
        }
        return messageQueues;
    }
}
