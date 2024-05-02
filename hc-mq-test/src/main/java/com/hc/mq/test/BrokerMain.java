package com.hc.mq.test;

import com.hc.mq.server.core.Broker;

/**
 * @Author hc
 */
public class BrokerMain {
    public static void main(String[] args) throws Exception {
        Broker broker = Broker.init().start();
    }
}
