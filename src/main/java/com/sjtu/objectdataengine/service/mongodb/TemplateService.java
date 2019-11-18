package com.sjtu.objectdataengine.service.mongodb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.redis.RedisTemplateService;
import com.sjtu.objectdataengine.utils.TypeConversion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

@Component
public class TemplateService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisTemplateService redisTemplateService;

    public String create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        String name = jsonObject.getString("name");
        if (name == null) name = "";
        String type = jsonObject.getString("type");
        if (type == null) return "类型不能为空";
        String nodeId = jsonObject.getString("nodeId");
        if (nodeId == null) nodeId = "";
        JSONObject attrsJson = jsonObject.getJSONObject("attrs");
        if (attrsJson == null) return "属性不能为空";
        HashMap<String, String> attrs = TypeConversion.JsonToMap(attrsJson);
        //redisTemplateService.createTemplate(id, name, type, nodeId, attrs);
        return "创建失败!";
    }

    public String delete() {
        return null;
    }

    public String modify() {
        return null;
    }

    public String bindNode(String NodeId, String template) {
        return null;
    }
}
