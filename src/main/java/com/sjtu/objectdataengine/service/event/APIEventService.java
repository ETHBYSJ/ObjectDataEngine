package com.sjtu.objectdataengine.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.subscribe.BaseSubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.TemplateBaseSubscribeMessage;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.TemplateSubscribeService;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class APIEventService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisEventService redisEventService;

    @Resource
    RedisTemplateService redisTemplateService;

    @Resource
    MongoEventService mongoEventService;

    @Resource
    SubscribeService subscribeService;

    @Resource
    SubscribeSender subscribeSender;

    @Resource
    TemplateSubscribeService templateSubscribeService;


    /**
     * 创建一个事件
     * id, name, intro, template都必须是不为空的
     * @param request 请求体
     * @return 结果描述
     */
    public String create(String request) {
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
        else if (!objectTemplate.getType().equals("event")) return "模板不是事件模板！";

        JSONObject attrObject = jsonObject.getJSONObject("attrs");
        HashMap<String, String> attrs = new HashMap<>();
        if (attrObject != null) {
            for (Map.Entry entry : attrObject.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                attrs.put(key, value);
            }
        }
        Date date = new Date();
        if (redisEventService.create(id, name, intro, template, attrs, date) && mongoEventService.create(id, name, intro, template, attrs, date)) {
            // 通知模板订阅者
            Map<String, Object> map = new HashMap<>();
            map.put("op", "OBJECT_CREATE_NOTICE");
            map.put("message", "基于模板(ID=" + template + ")创建了新的实体对象，对象ID为" + id);
            map.put("type", "entity");
            map.put("object", redisEventService.findEventObjectById(id));
            Set<String> subscriberSet = getSubscriberSet(template);
            for (String subscriber : subscriberSet) {
                subscribeSender.send(JSON.toJSONString(map), subscriber);
            }
            return "创建成功";
        }
        return "创建失败";
    }

    /**
     * 删除一个事件
     * @param id eventId
     * @return 结果说明
     */
    public String delete(String id) {
        if(id == null || id.equals("")) return "ID不能为空";

        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) return "没有该事件";

        String template = eventObject.getTemplate();

        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "EVENT_DELETE");
        message.put("event", id);
        message.put("template", template);
        // 通知关联的实体对象订阅者
        mongoSender.send(message);
        if (redisEventService.deleteEventById(id, template) && mongoEventService.deleteEventById(id, template)) {
            return  "删除失败！";
        }
        return "删除成功！";
    }

    public String modifyBase(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "EVENT_MODIFY_BASE");
        // id必须要有
        String id = jsonObject.getString("id");
        if (id == null || id.equals("")) return "ID不能为空";
        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) return "事件不存在或已结束";     // 不存在
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if (name != null) {
            modifyMessage.put("name", name);
        }
        // intro如果是null就不需要改
        String intro = jsonObject.getString("intro");
        if (intro != null) {
            modifyMessage.put("intro", intro);
        }
        // stage如果是null就不需要改
        String stage = jsonObject.getString("stage");
        if (stage != null) {
            modifyMessage.put("stage", stage);
        }
        // 日期
        Date date = new Date();
        modifyMessage.put("date", date);
        mongoSender.send(modifyMessage);
        if (redisEventService.updateBaseInfo(id, name, intro, stage, date)) {
            return "修改成功";
        }
        return "修改失败";
    }

    public String end(String id) {
        if (id == null || id.equals("")) return "ID不能为空";

        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) return "没有该事件或事件已经结束";

        HashMap<String, Object> endMessage = new HashMap<>();

        Date date = new Date();
        endMessage.put("op", "EVENT_END");
        endMessage.put("id", id);
        endMessage.put("date", date);

        mongoSender.send(endMessage);

        // 删除redis中的已结束事件
        if (redisEventService.deleteEventById(id, eventObject.getTemplate())) {
            Map<String, Object> map = new HashMap<>();
            // 通知模板订阅者
            List<String> objects = eventObject.getObjects();
            BaseSubscribeMessage baseSubscribeMessage;
            String msg;
            List<String> userList;
            /*
            String template = eventObject.getTemplate();
            String msg = "基于模板(ID=" + template + ")创建的事件结束，事件ID为" + id;
            map.put("msg", msg);
            map.put("event", id);
            map.put("template", template);
            BaseSubscribeMessage baseSubscribeMessage = subscribeService.findByIdAndType(template, "template");
            if(baseSubscribeMessage != null) {
                userList = baseSubscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(JSON.toJSONString(map), user);
                }
            }
            */
            /*
            for(String object : objects) {
                baseSubscribeMessage = subscribeService.findByIdAndType(object, "entity");
                msg = "实体对象(ID=" + object + ")关联的事件结束，事件ID为 " + id;
                userList = baseSubscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    map = new HashMap<>();
                    map.put("msg", msg);
                    map.put("event", id);
                    map.put("id", object);
                    subscribeSender.send(JSON.toJSONString(map), user);
                }
            }

             */
            return "事件结束成功";
        }
        return "事件结束失败";
    }

    public EventObject find(String id) {
        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) {
            return mongoEventService.findEventObjectById(id);
        }
        return eventObject;
    }

    public String modifyAttr(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "EVENT_MODIFY_ATTR");
        // id必须要有
        String id = jsonObject.getString("id");
        if (id == null || id.equals("")) return "ID不能为空";
        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) return "事件不存在或已结束";     // 不存在
        // 属性name
        String name = jsonObject.getString("name");
        if (name == null || name.equals("")) return "属性name不能为空";
        modifyMessage.put("name", name);
        // 属性value
        String value = jsonObject.getString("value");
        if (value == null) return "属性值不能为空";
        modifyMessage.put("value", value);
        // 日期date
        Date date = new Date();
        modifyMessage.put("date", date);
        mongoSender.send(modifyMessage);
        if (redisEventService.updateAttr(id, name, value, date)) {
            return  "更新属性成功";
        }
        return "更新属性失败";
    }

    /**
     * 获得订阅者集合
     * @param template 模板id
     * @return 订阅者集合
     */
    private Set<String> getSubscriberSet(String template) {
        // 发送列表
        Set<String> subscriberSet = new HashSet<>();
        // 模板订阅者
        TemplateBaseSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(template);
        if(templateSubscribeMessage != null) {
            HashMap<String, List<String>> templateSubscriberList = templateSubscribeMessage.getTemplateSubscriber();
            for(String templateSubscriber : templateSubscriberList.keySet()) {
                subscriberSet.add(templateSubscriber);
            }
        }
        return subscriberSet;
    }
}
