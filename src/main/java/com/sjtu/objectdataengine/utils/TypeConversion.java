package com.sjtu.objectdataengine.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Set;

public class TypeConversion {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object obj) {
        return (T) obj;
    }

    public static HashMap<String, String> JsonToMap(JSONObject jsonObject){
        HashMap<String, String> data = new HashMap<>();
        Set<String> keySet = jsonObject.keySet();
        for (String key : keySet) {
            data.put(key, jsonObject.get(key).toString());
        }
        return data;
    }
}
