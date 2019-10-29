package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.RedisSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataService {

    @Autowired
    MongoSender mongoSender;

    @Autowired
    RedisSender redisSender;

    @Autowired
    MongoTemplateService mongoTemplateService;

    /**
     * 创建对象
     * @param request json请求
     * @return String
     */
    public String create(String request) {
        //解析JSON
        JSONObject jsonObject = JSON.parseObject(request);
        String id = jsonObject.getString("id");
        if (id == null) return "ID不能为空！";

        String template = jsonObject.getString("template");
        if(template == null) return "template不能为空！";
        else if (mongoTemplateService.findTemplateById(template)==null) return "template不存在";

        JSONArray objectsArray = jsonObject.getJSONArray("objects");
        List<String> objects = new ArrayList<>();
        if (objectsArray != null) {
            objects = JSONObject.parseArray(objectsArray.toJSONString(), String.class);
        }

        JSONObject attrObject = jsonObject.getJSONObject("attrs");
        HashMap<String, String> attrs = new HashMap<>();
        if (attrObject != null) {
            for(Map.Entry entry : attrObject.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                attrs.put(key, value);
            }
        }

        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "CREATE");
        message.put("id", id);
        message.put("template", template);
        message.put("objects", objects);
        message.put("attrs", attrs);

        mongoSender.send(message);
        redisSender.send(message);

        return "创建成功！";
    }

    public void findObjectByKey(String id) {

    }

}
