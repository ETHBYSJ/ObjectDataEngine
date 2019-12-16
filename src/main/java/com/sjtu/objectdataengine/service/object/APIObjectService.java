package com.sjtu.objectdataengine.service.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.subscribe.EntitySubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.TemplateSubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.event.RedisEventService;
import com.sjtu.objectdataengine.service.subscribe.EntitySubscribeService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.TemplateSubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
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

    @Resource
    TemplateSubscribeService templateSubscribeService;

    @Resource
    EntitySubscribeService entitySubscribeService;

    @Resource
    UserService userService;

    /**
     * 创建对象
     * @param jsonObject json请求
     * @return String
     */
    public String create(JSONObject jsonObject) {
        //解析JSON
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
        Map<String, Object> map = new HashMap<>();
        // 通知模板订阅者
        String MSG = "基于模板(ID=" + template + ")创建了新的实体对象，对象ID为" + id;
        map.put("op", "OBJECT_CREATE_NOTICE");
        map.put("message", MSG);
        map.put("type", "entity");
        map.put("object", redisObjectService.findObjectById(id));
        TemplateSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(template);
        if(templateSubscribeMessage != null) {
            List<String> userList = templateSubscribeMessage.getObjectSubscriber();
            for(String user : userList) {
                subscribeSender.send(JSON.toJSONString(map), user);
            }
        }
        return msg;
    }

    /**
     * 重载创建函数
     * @param request 请求
     * @return 说明信息
     */
    public String create(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        return create(jsonObject);
    }

    public String addAttr(String id, String name, String value) {
        if (id == null || id.equals("")) return "ID不能为空！";
        if (name == null || name.equals("")) return "name不能为空";
        if (value == null) return "value不能为空";
        CommonObject mongoObject = mongoObjectService.findLatestObjectByKey(id);
        if (mongoObject == null) return "ID不存在";
        String template = mongoObject.getTemplate();
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
        // 关联事件
        List<String> events = new ArrayList<>(mongoObject.getEvents().keySet());

        EntitySubscribeMessage entitySubscribeMessage = entitySubscribeService.findById(id);
        Map<String, Object> map = new HashMap<>();
        String MSG = "对象(ID=" + id + ")已经被删除";
        map.put("op", "OBJECT_UPDATE_NOTICE");
        map.put("message", MSG);
        map.put("id", id);
        map.put("updateTime", date);
        map.put("value", value);
        map.put("name", name);
        // 暂时未去重
        if(entitySubscribeMessage != null) {
            List<String> entitySubscriberList = entitySubscribeMessage.getObjectSubscriber();
            HashMap<String, List<String>> attrSubscriberMap = entitySubscribeMessage.getAttrsSubscriber();
            // 给实体对象订阅者发消息
            for(String subscriber : entitySubscriberList) {
                subscribeSender.send(JSON.toJSONString(map), subscriber);
            }
            // 给属性订阅者发消息
            for(Map.Entry<String, List<String>> entry : attrSubscriberMap.entrySet()) {
                List<String> attrSubscriberList = entry.getValue();
                for(String attrSubscriber : attrSubscriberList) {
                    subscribeSender.send(JSON.toJSONString(map), attrSubscriber);
                }
            }
        }
        // 实体对象模板订阅者
        TemplateSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(template);
        if(templateSubscribeMessage != null) {
            Map<String, Object> map1 = new HashMap<>();
            String MSG1 = "基于模板(ID=" + template + ")创建的对象(ID=" + id + ")更新了一条属性，属性名为(" + name + ")属性值为(" + value + ")";
            map1.put("op", "OBJECT_UPDATE_NOTICE");
            map1.put("message", MSG1);
            map1.put("id", id);
            map1.put("updateTime", date);
            map1.put("value", value);
            map1.put("name", name);
            List<String> templateSubscriberList = templateSubscribeMessage.getObjectSubscriber();
            for(String templateSubscriber : templateSubscriberList) {
                subscribeSender.send(JSON.toJSONString(map1), templateSubscriber);
            }
        }
        // 关联的事件关联的模板订阅者
        List<User> userList= userService.findAll();
        if(userList != null) {
            for(User user : userList) {
                HashMap<String, List<String>> inverseEvents = user.getInverseEvents();
                for(String event : events) {
                    List<String> templateList = inverseEvents.get(event);
                    if(templateList != null) {
                        for(String t : templateList) {
                            Map<String, Object> map2 = new HashMap<>();
                            String MSG2 = "模板(ID=" + t + ")关联的事件(ID=" + event + ")相关的对象(ID=" + id + ")更新了一条属性，属性名为(" + name + ")属性值为(" + value + ")";
                            map2.put("op", "OBJECT_DELETE_NOTICE");
                            map2.put("message", MSG2);
                            map2.put("id", id);
                            map2.put("updateTime", date);
                            map2.put("value", value);
                            map2.put("name", name);
                        }
                    }
                }
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
        if (id == null || id.equals("")) return "ID不能为空";
        CommonObject mongoObject = mongoObjectService.findLatestObjectByKey(id);
        CommonObject redisObject = redisObjectService.findObjectById(id);
        List<String> events = new ArrayList<>();
        String template = "";
        if (mongoObject == null && redisObject == null) return "ID不存在";
        if (mongoObject != null) {
            events = new ArrayList<String>(mongoObject.getEvents().keySet());
            template = mongoObject.getTemplate();
            if(!mongoObjectService.deleteObjectById(id, mongoObject.getTemplate())) {
                return "删除失败";
            }
        }
        if (redisObject != null) {
            events = new ArrayList<String>(redisObject.getEvents().keySet());
            template = redisObject.getTemplate();
            if(!redisObjectService.deleteObjectById(id, redisObject.getTemplate())) {
                return "删除失败";
            }
        }
        EntitySubscribeMessage entitySubscribeMessage = entitySubscribeService.findById(id);
        // 暂时未去重
        if(entitySubscribeMessage != null) {
            Map<String, Object> map = new HashMap<>();
            String MSG = "对象(ID=" + id + ")已经被删除";
            map.put("op", "OBJECT_DELETE_NOTICE");
            map.put("message", MSG);
            map.put("id", id);
            List<String> entitySubscriberList = entitySubscribeMessage.getObjectSubscriber();
            HashMap<String, List<String>> attrSubscriberMap = entitySubscribeMessage.getAttrsSubscriber();
            // 给实体对象订阅者发消息
            for(String subscriber : entitySubscriberList) {
                subscribeSender.send(JSON.toJSONString(map), subscriber);
            }
            // 给属性订阅者发消息
            for(Map.Entry<String, List<String>> entry : attrSubscriberMap.entrySet()) {
                List<String> attrSubscriberList = entry.getValue();
                for(String attrSubscriber : attrSubscriberList) {
                    subscribeSender.send(JSON.toJSONString(map), attrSubscriber);
                }
            }
        }
        // 实体对象模板订阅者
        TemplateSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(template);
        if(templateSubscribeMessage != null) {
            Map<String, Object> map1 = new HashMap<>();
            String MSG1 = "基于模板(ID=" + template + ")创建的对象(ID=" + id + ")已经被删除";
            map1.put("op", "OBJECT_DELETE_NOTICE");
            map1.put("message", MSG1);
            map1.put("id", id);
            List<String> templateSubscriberList = templateSubscribeMessage.getObjectSubscriber();
            for(String templateSubscriber : templateSubscriberList) {
                subscribeSender.send(JSON.toJSONString(map1), templateSubscriber);
            }
        }
        // 关联的事件关联的模板订阅者
        List<User> userList= userService.findAll();
        if(userList != null) {
            for(User user : userList) {
                HashMap<String, List<String>> inverseEvents = user.getInverseEvents();
                for(String event : events) {
                    List<String> templateList = inverseEvents.get(event);
                    if(templateList != null) {
                        for(String t : templateList) {
                            Map<String, Object> map2 = new HashMap<>();
                            String MSG2 = "模板(ID=" + t + ")关联的事件(ID=" + event + ")相关的对象(ID=" + id + ")已经被删除";
                            map2.put("op", "OBJECT_DELETE_NOTICE");
                            map2.put("message", MSG2);
                            map2.put("id", id);
                        }
                    }
                }
            }
        }
        subscribeService.deleteByIdAndType(id, "entity");
        return "删除成功";
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
