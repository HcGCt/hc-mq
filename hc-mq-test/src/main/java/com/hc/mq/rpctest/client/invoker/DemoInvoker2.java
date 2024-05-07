package com.hc.mq.rpctest.client.invoker;

import com.hc.mq.rpctest.client.service.DemoService2;
import com.hc.rpc.invoker.CallType;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;

/**
 * @Author hc
 */
public class DemoInvoker2 {
    private static RpcReferenceBean rpcReferenceBean = RpcInvokerFactory.createRpcReferenceBean(10000000);
    private static DemoService2 service2;
    public static void main(String[] args) throws Exception {
        testSYN();
    }

    public static void testSYN() throws Exception {
        rpcReferenceBean.setCallType(CallType.SYNC);
        service2 = rpcReferenceBean.getObject(DemoService2.class);
        String hello = service2.sayHello("王五");
        System.out.println(hello);
        RpcInvokerFactory.stop();
    }
}
