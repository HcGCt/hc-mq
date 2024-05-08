package com.hc.mq.server.core.replication;


import com.hc.mq.common.comm.MqException;
import com.hc.mq.common.config.MqServerConfig;
import com.hc.mq.common.message.Message;
import com.hc.mq.common.remoting.service.IMqService;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.invoker.RpcInvokerFactory;
import com.hc.rpc.invoker.RpcReferenceBean;
import com.hc.rpc.registry.IRegistryCenter;
import com.hc.rpc.registry.RegistryFactory;
import com.hc.rpc.utils.IpUtil;
import com.hc.rpc.utils.RpcStringUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.hc.mq.server.core.memo.MemoData.waitStoreReplicateMessages;

/**
 * 多broker实例,异步复制消息
 *
 * @Author hc
 */
public class ReplicateService {
    // 异步添加/删除消息
    private ScheduledExecutorService replicateExecutorService;
    RpcReferenceBean replicateReferenceBean;
    private IMqService mqService;
    private final String localAddress;

    private static ReplicateService instance = new ReplicateService();

    public static ReplicateService getInstance() {
        return instance;
    }

    private ReplicateService() {
        localAddress = IpUtil.getIpPort(MqServerConfig.getInstance().getIp() != null ? MqServerConfig.getInstance().getIp() : IpUtil.getIp(), MqServerConfig.getInstance().getPort());
        replicateExecutorService = Executors.newScheduledThreadPool(2);
        replicateReferenceBean = RpcInvokerFactory.createRpcReferenceBean(30000000);
    }

    private long delay = 20;    // 20s

    public void start() {
        // 添加消息
        replicateExecutorService.scheduleWithFixedDelay(() -> {
            replicateMessage(true);
        }, delay, delay, TimeUnit.SECONDS);

        // 删除消息需要同步更新
        // replicateExecutorService.scheduleWithFixedDelay(() -> {
        //     replicateMessage(false);
        // }, delay, delay, TimeUnit.SECONDS);
    }

    private void replicateMessage(boolean store) {
        // LinkedBlockingQueue<Message> waitReplicateMessages = store ? waitStoreReplicateMessages : waitDeleteReplicateMessages;
        try {
            // waitReplicateMessages 的size为空则会被阻塞
            if (waitStoreReplicateMessages.isEmpty()) return;
            Message message = waitStoreReplicateMessages.poll();
            List<Message> messages = new ArrayList<>();
            messages.add(message);
            List<Message> otherMessageList = new ArrayList<>();
            int drain = waitStoreReplicateMessages.drainTo(otherMessageList, 100);
            if (drain > 0) {
                messages.addAll(otherMessageList);
            }
            messages = messages.stream().map(msg -> {
                Message newMessage = new Message(msg);
                newMessage.setStored(!store);
                return newMessage;
            }).collect(Collectors.toList());

            List<String> otherBrokersAddress = getOtherBrokersAddress();
            for (String address : otherBrokersAddress) {
                replicateReferenceBean.setServerAddress(address);
                mqService = replicateReferenceBean.getObject(IMqService.class);
                mqService.replicateToStore(messages);
                // if (store) {
                //
                // } else {
                //     mqService.replicateToDelete(messages);
                // }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteMessage(String msgId) {
        try {
            List<String> otherBrokersAddress = getOtherBrokersAddress();
            for (String address : otherBrokersAddress) {
                replicateReferenceBean.setServerAddress(address);
                mqService = replicateReferenceBean.getObject(IMqService.class);
                mqService.replicateToDelete(msgId);
            }
        } catch (Exception e) {
            throw new MqException(e);
        }
    }

    private List<String> getOtherBrokersAddress() {
        String key = RpcStringUtil.buildProviderKey(IMqService.class.getSimpleName(), RpcConfig.getInstance().getVersion());
        IRegistryCenter registryCenter = RegistryFactory.get(RpcConfig.getInstance().getRegisterType());
        List<ProviderMeta> providerMetas = registryCenter.discoveries(key);
        return providerMetas.stream()
                .map(ProviderMeta::getAddress)
                .filter(addr -> !addr.equals(localAddress))
                .collect(Collectors.toList());
    }

    public void stop() {
        if (!replicateExecutorService.isShutdown()) {
            synchronized (this) {
                replicateExecutorService.shutdown();
            }
        }
    }
}
