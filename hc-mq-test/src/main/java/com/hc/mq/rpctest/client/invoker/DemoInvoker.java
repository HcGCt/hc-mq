package com.hc.mq.rpctest.client.invoker;

import com.hc.mq.client.client.IMqService;
import com.hc.mq.client.common.SendResult;
import com.hc.mq.client.message.Message;
import com.hc.mq.rpctest.client.dto.Student;
import com.hc.mq.rpctest.client.service.DemoService;
import com.hc.rpc.invoker.CallType;
import com.hc.rpc.invoker.RpcInvokeCallback;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;

import java.util.Arrays;


/**
 * @Author hc
 */
public class DemoInvoker {
    private static RpcReferenceBean rpcReferenceBean = RpcInvokerFactory.createRpcReferenceBean(10000000);
    // rpcReferenceBean.setService(DemoService.class);
    private static DemoService service;
    public static void main(String[] args) throws InterruptedException {
        try {
            // testSYN();
            // testCallback();

            testSendMessageSYN();
            testSendMessageCallback();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            Thread.sleep(3000);
            RpcInvokerFactory.stop();
        }

    }


    public static void testSYN() throws Exception {
        rpcReferenceBean.setCallType(CallType.SYNC);
        service = rpcReferenceBean.getObject(DemoService.class);
        Student student = service.getStudent("张三");
        System.out.println(student);
    }

    public static void testCallback() throws Exception {
        // rpcReferenceBean.setServerAddress("192.168.88.1:9999");
        rpcReferenceBean.setCallType(CallType.CALLBACK);
        service = rpcReferenceBean.getObject(DemoService.class);

        RpcInvokeCallback.setCallback(new RpcInvokeCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("回调回调回调回调结果：" + result);
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println(exception.getMessage());
            }
        });

        String hello = service.sayHello("李四");
    }


    private static IMqService mqService;
    public static void testSendMessageSYN() throws Exception {
        rpcReferenceBean.setCallType(CallType.SYNC);
        mqService = rpcReferenceBean.getObject(IMqService.class);
        Message message = new Message("ssss","asfasfas".getBytes());
        SendResult sendResult = mqService.sendMessages(Arrays.asList(message));
        System.out.println(sendResult.getResponseBroker());
    }

    public static void testSendMessageCallback() throws Exception {
        rpcReferenceBean.setCallType(CallType.CALLBACK);
        mqService = rpcReferenceBean.getObject(IMqService.class);

        RpcInvokeCallback.setCallback(new RpcInvokeCallback() {
            @Override
            public void onSuccess(Object result) {
                System.out.println("回调回调回调回调结果：" + result);
            }

            @Override
            public void onFailure(Throwable exception) {
                System.out.println(exception.getMessage());
            }
        });
        Message message = new Message("ssss","asfasfas".getBytes());
        SendResult sendResult = mqService.sendMessages(Arrays.asList(message));
    }
}
