package com.hc.mq.dashboard.service;

import com.hc.mq.dashboard.entity.Registry;
import com.hc.rpc.common.ProviderMeta;

import java.util.List;

/**
 * @Author hc
 */
public interface IRegistryService {

    Registry getRegistryById(int id);
    Registry getRegistryByHash(int hashCode);

    int add(String key, int hashCode, String registryInfo);

    int update(Registry registry);

    int removeById(int id);


    List<ProviderMeta> getRegistriesByKey(String key);

    int removeByInfo(String key, String info);

}
