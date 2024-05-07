package com.hc.mq.dashboard;

import com.hc.mq.client.message.Message;
import com.hc.mq.dashboard.dao.IRegistryDao;
import com.hc.mq.dashboard.entity.Registry;
import com.hc.mq.dashboard.remoting.RemoteServerReference;
import com.hc.mq.dashboard.service.IRegistryService;
import com.hc.mq.client.util.JsonUtil;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.utils.UUIDUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @Author hc
 */
@SpringBootTest
public class Tests {

    @Autowired
    private IRegistryService registryService;
    @Autowired
    private IRegistryDao registryDao;

    @Test
    public void test() {
        List<Registry> list = registryDao.list();
        Registry registry = registryService.getRegistryById(7);
        System.out.println(registry.getRegistryInfo());
        int id = registry.getId();
        int i = registryService.removeById(id);
        System.out.println(i);
    }

    @Test
    public void testDelete() {
        String key = "testKey";
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setVersion("99.0");
        providerMeta.setAddress("111.111.111.111:1");
        providerMeta.setEndTime(System.currentTimeMillis() + 121412412L);
        providerMeta.setName("测试");
        providerMeta.setUUID(UUIDUtils.createUUID16());
        String info = JsonUtil.convertObj2Json(providerMeta);
        // int add = registryService.add(key, info);
        // System.out.println(add);
    }

    @Test
    public void testDiscovery() {
        String key = "IMqService$1.0";
        List<ProviderMeta> registriesByKey = registryService.getRegistriesByKey(key);
        System.out.println();
    }

    @Test
    public void testAdd() {
        String key = "test_key";
        int hashCode = 124125;
        ProviderMeta providerMeta = new ProviderMeta();
        providerMeta.setUUID("2dasda");
        providerMeta.setName("测试名");
        providerMeta.setVersion("44.0");
        providerMeta.setAddress("199912.42141924.421412");
        int add = registryService.add(key, hashCode, JsonUtil.convertObj2Json(providerMeta));
        System.out.println(add);
    }

    @Test
    public void testUpdate() {
        int hashcode = -1103296310;
        Registry registryByHash = registryService.getRegistryByHash(hashcode);
        registryByHash.setRegistryKey("喜喜");
        registryService.update(registryByHash);
    }


    @Autowired
    private RemoteServerReference remoteServerReference;

    @Test
    public void testGetMessageFromBroker() {
        List<Message> messages = remoteServerReference.listMessagesByTopic("testTopic1");

        messages.forEach(msg -> System.out.println(msg.getMsgId()));
    }
}
