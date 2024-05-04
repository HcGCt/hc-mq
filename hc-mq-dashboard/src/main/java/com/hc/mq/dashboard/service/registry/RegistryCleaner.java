package com.hc.mq.dashboard.service.registry;

import com.hc.mq.dashboard.dao.IRegistryDao;
import com.hc.mq.dashboard.entity.Registry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author hc
 */
@Component
public class RegistryCleaner implements BeanPostProcessor , DisposableBean {
    private ScheduledExecutorService scheduledCleaner = Executors.newSingleThreadScheduledExecutor();

    @Autowired
    private IRegistryDao registryDao;

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        long delay = 30;
        long ttl = 60 * 1000;
        scheduledCleaner.scheduleWithFixedDelay(() -> {
            List<Registry> list = registryDao.list();
            for (Registry registry : list) {
                if (registry.getUpdateTime().getTime() + ttl < System.currentTimeMillis()) {
                    registryDao.delete(registry.getId());
                }
            }
        }, delay, delay, TimeUnit.SECONDS);
        return bean;
    }

    @Override
    public void destroy() throws Exception {
        scheduledCleaner.shutdown();
    }
}
