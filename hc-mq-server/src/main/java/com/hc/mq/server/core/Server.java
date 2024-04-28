package com.hc.mq.server.core;

import com.hc.mq.client.client.IMqService;
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
        providerFactory.setServerHost(ip);
        providerFactory.setServerPort(port);
        // providerFactory.setRegisterAddress();        // 注册中心todo
        // providerFactory.setRegisterType();
        System.out.println("添加服务");
        providerFactory.addService(IMqService.class.getSimpleName(),"1.0", new MqServiceImpl());

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
