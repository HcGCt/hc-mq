package com.hc.mq.server.core;

import com.hc.mq.client.client.IMqService;
import com.hc.mq.dashboard.remoting.DashboardService;
import com.hc.mq.server.config.MqServerConfig;
import com.hc.mq.server.dashboard.DashboardServiceImpl;
import com.hc.rpc.config.RpcConfig;
import com.hc.rpc.provider.RpcProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author hc
 */
public class Server {

    private static Logger logger = LoggerFactory.getLogger(Server.class);

    private static volatile Server instance;

    private Server() {
    }

    public static Server getInstance() {
        if (instance == null) {
            synchronized (Server.class) {
                instance = new Server();
            }
        }
        return instance;
    }

    private static RpcProviderFactory providerFactory = new RpcProviderFactory();
    private static volatile boolean stopped = true;

    public Server init(String ip, int port) {
        RpcConfig.getInstance().setServerPort(port);
        providerFactory.setServerHost(ip);
        providerFactory.setServerPort(port);
        providerFactory.setRegisterAddress(MqServerConfig.getInstance().getRegisterAddress());
        providerFactory.addService(IMqService.class.getSimpleName(), RpcConfig.getInstance().getVersion(), new MqServiceImpl());

        // todo dashboard service
        providerFactory.addService(DashboardService.class.getSimpleName(), RpcConfig.getInstance().getVersion(),new DashboardServiceImpl());
        return this;
    }


    public Server start() throws Exception {
        if (stopped) {
            synchronized (Server.class) {
                logger.info("broker已启动, port: {}", providerFactory.getServerPort());
                providerFactory.start();
                stopped = false;
            }
        }
        return this;
    }

    public Server stop() throws Exception {
        if (!stopped) {
            synchronized (Server.class) {
                providerFactory.stop();
                stopped = true;
            }
        }
        return this;
    }
}
