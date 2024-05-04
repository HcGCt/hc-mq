package com.hc.mq.httptest;

import com.hc.mq.client.util.HttpUtil;
import com.hc.mq.client.util.JsonUtil;
import com.hc.rpc.common.ProviderMeta;
import com.hc.rpc.utils.RpcStringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author hc
 */
public class HttpTests {
    public static void main(String[] args) throws Exception {
        String url = "http://127.0.0.1:8080/registry/discovery";
        String key = "IMqService$1.0";
        Map<String, Object> params = new HashMap<>();
        params.put("key", key);
        String result = HttpUtil.doPost(url, params, null);
        List<ProviderMeta> providerMetas = JsonUtil.convertJsonArray2List(result, ProviderMeta.class);

        System.out.println(result);
    }

}
