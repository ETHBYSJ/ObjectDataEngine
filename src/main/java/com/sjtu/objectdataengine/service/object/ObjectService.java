package com.sjtu.objectdataengine.service.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.rabbitMQ.mongodb.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.redis.RedisSender;
import com.sjtu.objectdataengine.service.event.RedisEventService;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
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
    RedisTemplateService redisTemplateService;

    @Resource
    RedisEventService redisEventService;


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
        if (intro == null || intro.equals("")) return "intro不能为空！";

        String template = jsonObject.getString("template");
        if(template == null || template.equals("")) return "template不能为空！";
        else if (!redisTemplateService.hasKey(template)) return "template不存在";

        JSONArray eventsArray = jsonObject.getJSONArray("events");
        List<String> events = new ArrayList<>();
        if (eventsArray != null) {
            events = JSONObject.parseArray(eventsArray.toJSONString(), String.class);
        }

        // 检查events的合法性
        for (String event : events) {
            if (event == null || event.equals("")) return "events列表不合法";
            if (redisEventService.findEventObjectById(id) == null) return "eventId=" + event + "不存在";
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
        message.put("op", "OBJECT_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("intro", intro);
        message.put("template", template);
        message.put("events", events);
        message.put("attrs", attrs);

        mongoSender.send(message);
        redisSender.send(message);

        return "创建成功！";
    }

    public String addAttr(String id, String name, String value) {
        if (id == null || id.equals("")) return "ID不能为空！";
        if (name == null || name.equals("")) return "name不能为空";
        if (value == null) return "value不能为空";

        Date date = new Date();
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "OBJECT_ADD_ATTR");
        message.put("id", id);
        message.put("name", name);
        message.put("value", value);
        message.put("date", date);

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
    public List<CommonObject> findObjectsByTimes(String id, Date start, Date end) {
        return mongoObjectService.findObjectByStartAndEnd(id, start, end);
    }

    /**
     * 根据知识树结点id和事件id获取对应object的最新状态
     * @param nodeId 树结点Id
     * @param eventId 事件Id
     */
    public List<CommonObject> findObjectsByNodeAndEvent(String nodeId, String eventId) {
        // 合法性
        List<CommonObject> result = new ArrayList<>();
        if (nodeId == null || nodeId.equals("")) return result;
        TreeNode treeNode = redisTreeService.findNodeByKey(nodeId);
        if (treeNode == null) return result;
        if (eventId == null || eventId.equals("")) return result;
        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(treeNode.getTemplate());
        List<String> objList = objectTemplate.getObjects();
        for (String obj : objList) {
            CommonObject commonObject = this.findObjectByKey(obj);
            if(commonObject.getEvents().get(eventId)!=null) {
                result.add(commonObject);
            }
        }
        return result;
    }
}
