package com.hc.mq.client.registry;

import com.alibaba.fastjson.JSON;
import com.hc.mq.client.config.MqClientConfig;
import com.hc.mq.client.util.HttpUtil;
import com.hc.mq.client.util.JsonUtil;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.registry.IRegistryCenter;
import com.hc.rpc.utils.IpUtil;
import com.hc.rpc.utils.RpcStringUtil;
import com.hc.rpc.utils.UUIDUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author hc
 */
public class MqRegistryCenter implements IRegistryCenter {
    private static Logger logger = LoggerFactory.getLogger(MqRegistryCenter.class);
    private final String registerAddress;

    private final String registerUrl;
    private final String unRegisterUrl;
    private final String discoveryUrl;
    private final String heartbeatUrl;

    private Map<String, List<ProviderMeta>> brokerMetasCache;    // 服务注册信息本地缓存

    private String UUID;
    private Integer hashCode;
    // TODO 消息同步
    public MqRegistryCenter() {
        // ConcurrentHashMap,避免并发问题
        brokerMetasCache = new ConcurrentHashMap<>();
        hashCode = IpUtil.getIpPort(IpUtil.getLocalAddress().toString(), RpcConfig.getInstance().getServerPort()).hashCode();
        UUID = UUIDUtils.createUUID16();
        registerAddress = MqClientConfig.getInstance().getRegistryAddress();
        /** http url */
        registerUrl = "http://" + registerAddress + "/registry" + "/add";
        unRegisterUrl = "http://" + registerAddress + "/registry" + "/remove";
        discoveryUrl = "http://" + registerAddress + "/registry" + "/discovery";
        heartbeatUrl = "http://" + registerAddress + "/registry" + "/heartbeat";

        heartbeat();
        updateCache();
    }

    // ------------ 心跳监测/更新缓存 ------------
    private ScheduledExecutorService heartbeatExecutorService = Executors.newSingleThreadScheduledExecutor();
    private ScheduledExecutorService updateExecutorService = Executors.newSingleThreadScheduledExecutor();

    private void updateCache() {
        final long delay = 30;  // 30s检测一次
        updateExecutorService.scheduleWithFixedDelay(() -> {
            for (String key : brokerMetasCache.keySet()) {
                List<ProviderMeta> providerMetas = getBrokerMetas(key);
                brokerMetasCache.put(key, providerMetas);
            }
        }, delay, delay, TimeUnit.SECONDS);
    }

    private ProviderMeta curBrokerInfo;

    private void heartbeat() {
        final long delay = 10;  // 10s检测一次
        heartbeatExecutorService.scheduleWithFixedDelay(() -> {
            if (curBrokerInfo != null) {
                String key = RpcStringUtil.buildProviderKey(curBrokerInfo.getName(), curBrokerInfo.getVersion());
                Map<String, Object> params = new HashMap<>();
                String json = JsonUtil.convertObj2Json(curBrokerInfo);
                params.put("key", key);
                params.put("hashCode", hashCode);
                params.put("info", json);
                try {
                    HttpUtil.doPost(heartbeatUrl, params, null);
                } catch (Exception e) {
                    logger.error("心跳监测失败", e);
                }
            }
        }, delay, delay, TimeUnit.SECONDS);
    }


    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        curBrokerInfo = providerMeta;
        String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        providerMeta.setUUID(UUID);
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("hashCode", hashCode);
            String json = JsonUtil.convertObj2Json(providerMeta);
            params.put("info", json);
            HttpUtil.doPost(registerUrl, params, null);
            System.out.println("注册成功");
        } catch (Exception e) {
            logger.error("服务注册broker失败", e);
        }
    }

    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            String json = JsonUtil.convertObj2Json(providerMeta);
            params.put("info", json);
            HttpUtil.doPost(unRegisterUrl, params, null);
        } catch (Exception e) {
            logger.error("服务注销broker失败", e);
        }
    }

    @Override
    public List<ProviderMeta> discoveries(String key) {
        if (brokerMetasCache.containsKey(key)) {
            brokerMetasCache.get(key).size();
        }
        return getBrokerMetas(key);
    }

    @Override
    public void destroy() {
        heartbeatExecutorService.shutdown();
        updateExecutorService.shutdown();
    }


    private List<ProviderMeta> getBrokerMetas(String key) {
        List<ProviderMeta> brokerMetas = null;
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            String result = HttpUtil.doPost(discoveryUrl, params, null);
            brokerMetas = JsonUtil.convertJsonArray2List(result, ProviderMeta.class);
            brokerMetasCache.put(key, brokerMetas);
        } catch (Exception e) {
            logger.error("服务发现broker失败", e);
        }
        return brokerMetas;
    }

}
