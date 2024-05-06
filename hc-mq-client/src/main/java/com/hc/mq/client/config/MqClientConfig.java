package com.hc.mq.client.config;


import com.hc.rpc.utils.ConfigUtils;

import static com.hc.mq.client.common.Constants.DEFAULT_BROKER;
import static com.hc.mq.client.common.Constants.DEFAULT_CLIENT_CONFIG_PREFIX;


/**
 * @Author hc
 */
public class MqClientConfig {
    // 配置参数
    private String serverAddress = "127.0.0.1:9090";
    private String registryAddress = "127.0.0.1:8080";
    private String brokerName = DEFAULT_BROKER; // brokerName

    private static volatile MqClientConfig instance;
    private MqClientConfig(){}
    public static MqClientConfig getInstance() {
        if (instance == null) {
            synchronized (MqClientConfig.class) {
                try {
                    instance = ConfigUtils.loadConfig(MqClientConfig.class, DEFAULT_CLIENT_CONFIG_PREFIX);
                } catch (Exception e) {
                    // 加载失败使用默认配置
                    instance = new MqClientConfig();
                }
            }
        }
        return instance;
    }


    public String getServerAddress() {
        return serverAddress;
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }
}
