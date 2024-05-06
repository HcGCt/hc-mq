package com.hc.mq.server.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hc
 */
public class MqServerInitializer {

    private static Logger logger = LoggerFactory.getLogger(MqServerInitializer.class);

    private static volatile MqServerInitializer instance;

    private MqServerInitializer() {
    }

    public static MqServerInitializer getInstance() {
        if (instance == null) {
            synchronized (MqServerInitializer.class) {
                if (instance == null) {
                    instance = new MqServerInitializer();
                }
            }
        }
        return instance;
    }



}
