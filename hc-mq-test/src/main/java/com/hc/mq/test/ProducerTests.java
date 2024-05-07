package com.hc.mq.test;

import com.hc.mq.client.common.LocalTransactionState;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.config.MqClientConfig;
import com.hc.mq.client.config.MqClientInitializer;
import com.hc.mq.client.message.Message;
import com.hc.mq.client.producer.Producers;
import com.hc.mq.client.producer.SendCallback;
import com.hc.mq.client.producer.transaction.TransactionListener;
import com.hc.mq.client.util.BinaryUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author hc
 */
public class ProducerTests {

    public static void main(String[] args) throws Exception {
        System.out.println("===================生产者测试===================");
        MqClientInitializer.getInstance().start();

        testSendSYN();
        // testSendCallback();
        // testSendASYN();
        // testSendTransactionMessage();


        // while (!Thread.currentThread().isInterrupted()) {
        //     TimeUnit.MINUTES.sleep(2);
        // }


        MqClientInitializer.getInstance().stop();
    }


    public static void testSendSYN() {
        System.out.println("=========== sendSYN ===========");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 10; i++) {
            String msg = "test_msg_1_" + i;
            Message message = new Message("testTopic1", msg.getBytes(StandardCharsets.UTF_8));
            Producers.send(message, true);
        }
        System.out.println(" sendSYN 耗时: " + (System.currentTimeMillis() - start));
    }

    public static void testSendASYN() {
        System.out.println("=========== sendASYN ===========");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String msg = "test_msg_2_" + i;
            Message message = new Message("testTopic2", msg.getBytes(StandardCharsets.UTF_8));
            Producers.send(message, false);
        }
        System.out.println("sendASYN 耗时: " + (System.currentTimeMillis() - start));
    }


    // todo 回调响应阻塞？
    private static void testSendCallback() {

        System.out.println("=========== sendCallback ===========");
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            String msg = "test_msg_3_" + i;
            Message message = new Message("testTopic2", msg.getBytes(StandardCharsets.UTF_8));
            Producers.sendCallback(message, new SendCallback<SendResult>() {
                @Override
                public void onSuccess(SendResult result) {
                    System.out.println("测试sendCallback成功" + "响应broker:" + result.getResponseBroker());
                }

                @Override
                public void onException(Throwable e) {
                    System.out.println("测试sendCallback失败");
                }
            });
        }
        System.out.println("sendCallback 耗时: " + (System.currentTimeMillis() - start));
    }


    public static void testSendTransactionMessage() {
        System.out.println("=========== sendTransactionMessage ===========");
        long start = System.currentTimeMillis();
        List<Message> messages = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String msg = "test_tran_msg_2_" + i;
            Message message = new Message("testTopic5", msg.getBytes(StandardCharsets.UTF_8));
            messages.add(message);
        }
        Producers.sendMessagesInTransaction(messages, new TransactionListener() {
            @Override
            public LocalTransactionState executeLocalTransaction(List<Message> msgs, Object arg) {
                System.out.println("执行本地事务中+++++++++++");
                return LocalTransactionState.COMMIT_MESSAGE;
            }

            @Override
            public LocalTransactionState checkLocalTransaction(Message msg) {
                return LocalTransactionState.COMMIT_MESSAGE;
            }
        }, null);
        System.out.println("sendASYN 耗时: " + (System.currentTimeMillis() - start));
    }

}
