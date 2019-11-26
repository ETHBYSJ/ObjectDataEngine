package com.sjtu.objectdataengine.service.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.RedisSender;
import com.sjtu.objectdataengine.service.template.MongoTemplateService;
import com.sjtu.objectdataengine.service.tree.RedisTreeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class ObjectService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisSender redisSender;

    @Resource
    MongoObjectService mongoObjectService;

    @Resource
    RedisObjectService redisObjectService;

    @Resource
    RedisTreeService redisTreeService;

    @Resource
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
        if (id == null || id.equals("")) return "ID不能为空！";

        String name = jsonObject.getString("name");
        if (name == null || name.equals("")) return "name不能为空";
        String intro = jsonObject.getString("intro");
        if (intro == null) return "intro不能为空！";

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
        message.put("intro", intro);
        message.put("template", template);
        message.put("objects", objects);
        message.put("attrs", attrs);

        mongoSender.send(message);
        redisSender.send(message);

        return "创建成功！";
    }

    /**
     * 根据key获取最新object
     * @param id 对象id
     * @return 对象
     */
    public CommonObject findObjectByKey(String id) {
        CommonObject redisResult = redisObjectService.findObjectById(id);
        if (redisResult == null) {
            return mongoObjectService.findLatestObjectByKey(id);
        }
        return redisResult;
    }

    /**
     * 根据key获取对应time的object
     * @param id 对象id
     * @param time 时间
     * @return 对象
     */
    public CommonObject findObjectByTime(String id, Date time) {
        CommonObject redisResult = redisObjectService.findObjectById(id, time);
        if (redisResult == null) {
            return mongoObjectService.findObjectByTime(id, time);
        }
        return redisResult;
    }

    /**
     * 根据key获取对应时间段的object列表
     * @param id 对象id
     * @param start 开始时间
     * @param end 结束时间
     * @return 对象列表
     */
    public List<CommonObject> findObjectByTimes(String id, Date start, Date end) {
        return mongoObjectService.findObjectByStartAndEnd(id, start, end);
    }
}
