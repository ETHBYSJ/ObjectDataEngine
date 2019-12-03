package com.sjtu.objectdataengine.service.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.rabbitMQ.mongodb.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.redis.RedisSender;
import com.sjtu.objectdataengine.rabbitMQ.subscribe.SubscribeSender;
import com.sjtu.objectdataengine.service.event.RedisEventService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
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
    SubscribeSender subscribeSender;

    @Resource
    SubscribeService subscribeService;

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
        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(template);
        if(objectTemplate == null) return "template不存在";
        else if (!objectTemplate.getType().equals("entity")) return "模板不是实体模板！";

        JSONArray eventsArray = jsonObject.getJSONArray("events");
        List<String> events = new ArrayList<>();
        if (eventsArray != null) {
            events = JSONObject.parseArray(eventsArray.toJSONString(), String.class);
        } else {
            return "events必须指定（可为空列表)";
        }
        // 检查events的合法性
        for (String event : events) {
            if (event == null || event.equals("")) return "events列表不合法";
            if (redisEventService.findEventObjectById(event) == null) {
                return "eventId=" + event + "不存在";
            }
        }
        // attrs解析
        JSONObject attrObject = jsonObject.getJSONObject("attrs");
        HashMap<String, String> attrs = new HashMap<>();
        if (attrObject != null) {
            for(Map.Entry entry : attrObject.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                attrs.put(key, value);
            }
        }
        // 获取日期
        Date date = new Date();
        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "OBJECT_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("intro", intro);
        message.put("template", template);
        message.put("events", events);
        message.put("attrs", attrs);
        message.put("date", date);
        // 发送更新信息
        mongoSender.send(message);
        redisSender.send(message);
        // 创建订阅表
        subscribeService.create(id, "entity");
        // 通知模板订阅者
        final String msg1 = "基于模板(ID=" + template + ")创建了新的对象，对象ID为" + id;
        SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(template, "template");
        List<String> userList = subscribeMessage.getObjectSubscriber();
        for (String user : userList) {
            subscribeSender.send(msg1, user);
        }
        // 通知事件订阅者
        for (String event : events) {
            final String msg2 = "创建了与事件(ID=" + event + ")相关的新的对象，对象ID为" + id;
            subscribeMessage = subscribeService.findByIdAndType(event, "event");
            userList = subscribeMessage.getObjectSubscriber();
            for (String user : userList) {
                subscribeSender.send(msg2, user);
            }
        }
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

        final CommonObject commonObject = redisObjectService.findObjectById(id);

        // 通知属性和对象订阅者
        final String msg1 = "对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
        SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(id, "entity");
        List<String> userList = subscribeMessage.getAttrsSubscriber().get(name);
        // 去重复
        userList.addAll(subscribeMessage.getObjectSubscriber());
        HashSet<String> userSet = new HashSet<>(userList);
        for (String user : userSet) {
            subscribeSender.send(msg1, user);
        }

        // 通知事件订阅者
        Set<String> events = commonObject.getEvents().keySet();
        for (String event : events) {
            final String msg2 = "与事件(ID=" + event + ")关联的对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
            subscribeMessage = subscribeService.findByIdAndType(event, "event");
            if (subscribeMessage != null) {
                userList = subscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(msg2, user);
                }
            }
        }

        // 通知模板订阅者
        String template = commonObject.getTemplate();
        final String msg3 = "与模板(ID=" + template + ")关联的对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
        subscribeMessage = subscribeService.findByIdAndType(template, "template");
        userList = subscribeMessage.getObjectSubscriber();
        for (String user : userList) {
            subscribeSender.send(msg3, user);
        }
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

    public String addEventToObject(String id, String eventId) {
        /*if (id == null || id.equals("")) return "ID不能为空";
        else if ()
        if (eventId == null || eventId.equals("")) return "event不能为空";
        */
        return "增加失败";
    }
}
