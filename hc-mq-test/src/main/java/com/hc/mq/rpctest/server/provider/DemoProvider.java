package com.hc.mq.rpctest.server.provider;

import com.hc.mq.client.client.IMqService;
import com.hc.mq.rpctest.client.service.DemoService;
import com.hc.mq.rpctest.server.impl.DemoServiceImpl;
import com.hc.mq.server.core.MqServiceImpl;
import com.hc.rpc.provider.RpcProviderFactory;

/**
 * @Author hc
 */
public class DemoProvider {

    public static void main(String[] args) {
        RpcProviderFactory providerFactory = new RpcProviderFactory();
        providerFactory.setServerPort(7777);
        // providerFactory.setRegisterType("admin");
        // providerFactory.addService(DemoService.class.getSimpleName(), null, new DemoServiceImpl());
        providerFactory.addService(IMqService.class.getSimpleName(), null, new MqServiceImpl());

        providerFactory.start();

        // while (!Thread.currentThread().isInterrupted()) {
        //     TimeUnit.HOURS.sleep(1);
        // }

        // stop
        // providerFactory.stop();
    }
}
