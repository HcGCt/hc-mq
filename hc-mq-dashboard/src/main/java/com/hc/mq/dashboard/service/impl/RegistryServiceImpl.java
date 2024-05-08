package com.hc.mq.dashboard.service.impl;

import com.hc.mq.common.util.JsonUtil;
import com.hc.mq.dashboard.dao.IRegistryDao;
import com.hc.mq.dashboard.entity.Registry;
import com.hc.mq.dashboard.service.IRegistryService;
import com.hc.rpc.common.ProviderMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author hc
 */
@Service
public class RegistryServiceImpl implements IRegistryService {
    @Autowired
    private IRegistryDao registryDao;

    @Override
    public Registry getRegistryById(int id) {
        return registryDao.selectById(id);
    }

    @Override
    public Registry getRegistryByHash(int hashCode) {
        return registryDao.selectByHash(hashCode);
    }

    @Override
    public int add(String key, int hashCode, String registryInfo) {
        Registry registry = registryDao.selectByHash(hashCode);
        if (registry == null) {
            registry = new Registry();
            registry.setRegistryKey(key);
            registry.setRegistryInfo(registryInfo);
            registry.setHashCode(hashCode);
            registry.setUpdateTime(new Date());
            return registryDao.insert(registry);
        }
        return 1;
    }

    @Override
    public int update(Registry registry) {
        return registryDao.update(registry);
    }

    @Override
    public int removeById(int id) {
        return registryDao.delete(id);
    }


    @Override
    public List<ProviderMeta> getRegistriesByKey(String key) {
        List<Registry> registryList = registryDao.selectByKey(key);
        List<ProviderMeta> providerMetaList = registryList.stream().map(r -> {
            return JsonUtil.convertJson2Obj(r.getRegistryInfo(), ProviderMeta.class);
        }).collect(Collectors.toList());
        return providerMetaList;
    }

    @Override
    public int removeByInfo(String key, String info) {
        return registryDao.deleteByInfo(key, info);
    }




    // ---------- rpc服务接口? ----------
}
