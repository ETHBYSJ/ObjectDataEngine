package com.sjtu.objectdataengine.service.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.RedisSender;
import com.sjtu.objectdataengine.service.event.RedisEventService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.tree.RedisTreeService;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class APIObjectService {

    @Resource
    MongoSender mongoSender;

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
        String msg = "创建失败，请检查ID是否重复或网络出错";
        if (mongoObjectService.create(id, name, intro, template, attrs, events, date) && redisObjectService.create(id, name, intro, template, events, attrs, date)) {
            msg = "创建成功";
        } else {
            //mongoObjectService.deleteObjectById(id, template);
            return msg;
        }
        // 创建订阅表
        subscribeService.create(id, "entity");
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        // 通知模板订阅者
        final String msg1 = "基于模板(ID=" + template + ")创建了新的对象，对象ID为" + id;
        map1.put("msg", msg1);
        map1.put("template", template);
        map1.put("object", id);
        SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(template, "template");
        if(subscribeMessage != null) {
            List<String> userList = subscribeMessage.getObjectSubscriber();
            for (String user : userList) {
                subscribeSender.send(JSON.toJSONString(map1), user);
            }
            /*
            // 通知事件订阅者
            for (String event : events) {
                final String msg2 = "创建了与事件(ID=" + event + ")相关的新的对象，对象ID为" + id;
                map2.put("msg", msg2);
                subscribeMessage = subscribeService.findByIdAndType(event, "event");
                userList = subscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(map2, user);
                }
            }
            */
        }

        return msg;
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
        //redisSender.send(message);
        redisObjectService.addAttr(id, name, value, date);
        final CommonObject commonObject = redisObjectService.findObjectById(id);
        Map<String, Object> map1 = new HashMap<>();
        Map<String, Object> map2 = new HashMap<>();
        Map<String, Object> map3 = new HashMap<>();
        // 通知属性和对象订阅者
        final String msg1 = "对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
        map1.put("msg", msg1);
        map1.put("name", name);
        map1.put("object", id);
        map1.put("value", value);
        map1.put("date", date);
        SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(id, "entity");

        List<String> userList = subscribeMessage.getAttrsSubscriber().get(name);
        // 去重复
        userList.addAll(subscribeMessage.getObjectSubscriber());
        HashSet<String> userSet = new HashSet<>(userList);
        for (String user : userSet) {
            subscribeSender.send(JSON.toJSONString(map1), user);
        }
        /*
        // 通知事件订阅者
        Set<String> events = commonObject.getEvents().keySet();
        for (String event : events) {
            final String msg2 = "与事件(ID=" + event + ")关联的对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
            map2.put("msg", msg2);
            subscribeMessage = subscribeService.findByIdAndType(event, "event");
            if (subscribeMessage != null) {
                userList = subscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(map2, user);
                }
            }
        }
        */
        // 通知模板订阅者
        String template = commonObject.getTemplate();
        final String msg3 = "与模板(ID=" + template + ")关联的对象(" + "ID=" + id + ")的属性(" + name + ") 增加了一条新属性，属性值为 " + value + "\n更新时间：" + date;
        map3.put("msg", msg3);
        map3.put("name", name);
        map3.put("object", id);
        map3.put("value", value);
        map3.put("date", date);
        subscribeMessage = subscribeService.findByIdAndType(template, "template");
        if(subscribeMessage != null) {
            userList = subscribeMessage.getObjectSubscriber();
            for (String user : userList) {
                subscribeSender.send(JSON.toJSONString(map3), user);
            }
        }
        return "添加成功";
    }

    /**
     * 根据id删除对象
     * @param id ID
     * @return 删除情况
     */
    public String deleteObjectById(String id) {
        String msg = "删除成功";
        if (id == null || id.equals("")) return "ID不能为空";
        CommonObject mongoObject = mongoObjectService.findLatestObjectByKey(id);
        CommonObject redisObject = redisObjectService.findObjectById(id);
        if (mongoObject == null && redisObject == null) return "ID不存在";
        if (mongoObject != null) {
            if(!mongoObjectService.deleteObjectById(id, mongoObject.getTemplate())) {
                msg = "删除失败";
            }
        }
        if (redisObject != null) {
            if(!redisObjectService.deleteObjectById(id, redisObject.getTemplate())) {
                msg = "删除失败";
            }
        }
        return msg;
    }

    /**
     * 根据key获取最新object
     * @param id 对象id
     * @return 对象
     */
    public CommonObject findObjectById(String id) {
        CommonObject redisResult = redisObjectService.findObjectById(id);
        if (redisResult == null) {
            return mongoObjectService.findLatestObjectByKey(id);
        }
        return redisResult;
    }

    /**
     * 根据id name time获取指定时间的某条属性
     * @param id 对象id
     * @param name 属性name
     * @param time 时间
     * @return 属性值
     */
    public MongoAttr findAttrByTime(String id, String name, Date time) {
        MongoAttr redisResult = redisObjectService.findAttrByTime(id, name, time);
        if (redisResult == null) {
            return mongoObjectService.findAttrByTime(id, name, time);
        }
        return redisResult;
    }

    /**
     * 根据时间查找属性
     * @param id 对象id
     * @param name 属性name
     * @param start 开始时间
     * @param end 结束时间
     * @return 属性列表
     */
    public List<MongoAttr> findAttrByTimes(String id, String name, Date start, Date end) {
        if (start.after(end)) return null;
        return mongoObjectService.findAttrByStartAndEnd(id, name, start, end);
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
        System.out.println("from redis");
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
            CommonObject commonObject = this.findObjectById(obj);
            if(commonObject.getEvents().get(eventId)!=null) {
                result.add(commonObject);
            }
        }
        return result;
    }

    /**
     * 关联事件和对象
     * @param objId 对象id
     * @param eventId 事件id
     * @return 结果说明
     */
    public String bindEventAndObject(String objId, String eventId) {
        // 检查ID和事件ID存在与否
        if (objId == null || objId.equals("")) return "ID不能为空";
        else if (redisObjectService.findObjectById(objId) == null) return "ID不存在";
        if (eventId == null || eventId.equals("")) return "事件不能为空";
        else if (redisEventService.findEventObjectById(eventId) == null) return "事件ID不存在";
        // 组装信息
        Date date = new Date();
        HashMap<String, Object> bindMessage = new HashMap<>();
        bindMessage.put("objId", objId);
        bindMessage.put("eventId", eventId);
        bindMessage.put("date", date);
        // 添加事件id到对象的事件列表中
        redisObjectService.addEvent(objId, eventId, date);
        bindMessage.put("op", "OBJECT_ADD_EVENT");
        mongoSender.send(bindMessage);
        // 添加id到事件的对象列表中
        redisEventService.addObject(eventId, objId);
        bindMessage.put("op", "EVENT_ADD_OBJECT");
        mongoSender.send(bindMessage);

        return "增加成功";
    }
}
