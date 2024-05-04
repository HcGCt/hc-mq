package com.hc.mq.client.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Json工具类
 */
public class JsonUtil {
    private static final Logger logger = LoggerFactory.getLogger(JsonUtil.class);

    public static String convertObj2Json(Object obj) {
        return JSON.toJSONString(obj);
    }

    public static <T> T convertJson2Obj(String json, Class<T> clazz) {
        return JSONObject.parseObject(json, clazz);
    }

    public static <T> List<T> convertJsonArray2List(String json, Class<T> clazz) {
        return JSONArray.parseArray(json, clazz);
    }
}
