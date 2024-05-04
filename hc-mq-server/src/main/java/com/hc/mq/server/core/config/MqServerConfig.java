package com.hc.mq.server.core.config;

import com.hc.rpc.utils.ConfigUtils;


/**
 * @Author hc
 */
public class MqServerConfig {

    public static final String DEFAULT_SERVER_CONFIG_PREFIX = "hc.mq.server";

    private String ip;                          // 服务端ip
    private Integer port = 7070;                    // 服务端端口
    private Integer maxMessages = 2000;             // 队列最大容量
    private Double validMessageRatio = 0.3;     // 队列自动清理比例

    private String registerAddress = "127.0.0.1:8080";

    private static MqServerConfig instance;
    static {
        try {
            instance = ConfigUtils.loadConfig(MqServerConfig.class, DEFAULT_SERVER_CONFIG_PREFIX);
        } catch (Exception e) {
            // 加载失败使用默认配置
            instance = new MqServerConfig();
        }
    }
    public static MqServerConfig getInstance() {
        return instance;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Integer getMaxMessages() {
        return maxMessages;
    }

    public void setMaxMessages(Integer maxMessages) {
        this.maxMessages = maxMessages;
    }

    public Double getValidMessageRatio() {
        return validMessageRatio;
    }

    public void setValidMessageRatio(Double validMessageRatio) {
        this.validMessageRatio = validMessageRatio;
    }

    public String getRegisterAddress() {
        return registerAddress;
    }

    public void setRegisterAddress(String registerAddress) {
        this.registerAddress = registerAddress;
    }
}
