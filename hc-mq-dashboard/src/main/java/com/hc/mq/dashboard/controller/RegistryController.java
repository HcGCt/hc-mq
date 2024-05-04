package com.hc.mq.dashboard.controller;

import com.hc.mq.dashboard.entity.Registry;
import com.hc.mq.dashboard.service.IRegistryService;
import com.hc.rpc.common.ProviderMeta;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;
import java.util.List;

/**
 * @Author hc
 */
@Controller
@RequestMapping("registry")
public class RegistryController {
    @Autowired
    private IRegistryService registryService;


    @PostMapping("/add")
    @ResponseBody
    public String registry(@Param("key") String key, @Param("hashCode") Integer hashCode, @Param("info") String info) {
        int add = registryService.add(key, hashCode, info);
        System.out.println("注册成功");
        return "ok";
    }

    @PostMapping("/remove")
    @ResponseBody
    public String unRegistry(@Param("key") String key, @Param("info") String info) {
        registryService.removeByInfo(key, info);
        return "ok";
    }

    @PostMapping("/discovery")
    @ResponseBody
    public List<ProviderMeta> discovery(@Param("key") String key) {
        return registryService.getRegistriesByKey(key);
    }

    @PostMapping("/heartbeat")
    @ResponseBody
    public String heartbeat(@Param("key") String key, @Param("hashCode") Integer hashCode, @Param("info") String info) {
        Registry registry = registryService.getRegistryByHash(hashCode);
        if (registry == null) {
            registryService.add(key,hashCode,info);
            return "ok";
        }
        registry.setUpdateTime(new Date());
        registryService.update(registry);
        return "ok";
    }
}
