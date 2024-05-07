package com.hc.mq.rpctest.server.impl;

import com.hc.mq.rpctest.client.service.DemoService2;

/**
 * @Author hc
 */
public class DemoService2Impl implements DemoService2 {
    @Override
    public String sayHello(String name) {
        System.out.println("你好：" + name);
        return "我是DemoService2的响应";
    }
}
