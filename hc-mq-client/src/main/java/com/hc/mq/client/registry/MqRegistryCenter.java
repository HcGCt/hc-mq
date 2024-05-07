package com.hc.mq.client.registry;

import cn.hutool.core.collection.ConcurrentHashSet;
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
    protected final String registerAddress;

    protected final String registerUrl;
    protected final String unRegisterUrl;
    protected final String discoveryUrl;
    protected final String heartbeatUrl;
    private String brokerName;              // 注册brokerName

    private Map<String, List<ProviderMeta>> brokerMetasCache;    // 服务注册信息本地缓存

    protected String UUID;
    protected Integer hashCode;

    // TODO 消息同步
    public MqRegistryCenter() {
        // ConcurrentHashMap,避免并发问题
        brokerMetasCache = new ConcurrentHashMap<>();

        serviceSet = new ConcurrentHashSet<>();
        brokerName = MqClientConfig.getInstance().getBrokerName();
        hashCode = (IpUtil.getIpPort(IpUtil.getLocalAddress().toString(), RpcConfig.getInstance().getServerPort()) + brokerName).hashCode();
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

    // ------------ 本地服务集合(hashcode表示,一个Server可以注册多个服务) ------------
    protected Set<ProviderMeta> serviceSet;
    private void heartbeat() {
        final long delay = 10;  // 10s检测一次
        heartbeatExecutorService.scheduleWithFixedDelay(() -> {
            for (ProviderMeta brokerInfo : serviceSet) {
                if (brokerInfo != null) {
                    String key = RpcStringUtil.buildProviderKey(brokerInfo.getName(), brokerInfo.getVersion());
                    Map<String, Object> params = new HashMap<>();
                    String json = JsonUtil.convertObj2Json(brokerInfo);
                    params.put("key", key);
                    int hash = hashCode ^ brokerInfo.getName().hashCode();
                    params.put("hashCode", hash);
                    params.put("info", json);
                    try {
                        HttpUtil.doPost(heartbeatUrl, params, null);
                    } catch (Exception e) {
                        logger.error("心跳监测失败", e);
                    }
                }
            }
        }, delay, delay, TimeUnit.SECONDS);
    }


    @Override
    public void register(ProviderMeta providerMeta) throws Exception {
        try {
            String serviceName = providerMeta.getName();
            int hash = hashCode ^ serviceName.hashCode();
            String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
            providerMeta.setUUID(UUID);
            serviceSet.add(providerMeta);
            Map<String, Object> params = new HashMap<>();
            params.put("key", key);
            params.put("hashCode", hash);
            String json = JsonUtil.convertObj2Json(providerMeta);
            params.put("info", json);
            params.put("brokerName", brokerName);
            HttpUtil.doPost(registerUrl, params, null);
        } catch (Exception e) {
            logger.error("服务注册broker失败", e);
        }
    }

    @Override
    public void unRegister(ProviderMeta providerMeta) throws Exception {
        String key = RpcStringUtil.buildProviderKey(providerMeta.getName(), providerMeta.getVersion());
        try {
            serviceSet.remove(providerMeta);
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
        brokerMetasCache.clear();
        serviceSet.clear();
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
