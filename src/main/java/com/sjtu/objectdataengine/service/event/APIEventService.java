package com.sjtu.objectdataengine.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "EVENT_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("intro", intro);
        message.put("template", template);
        message.put("attrs", attrs);
        message.put("date", date);
        mongoSender.send(message);
        if (redisEventService.create(id, name, intro, template, attrs, date)) {
            Map<String, Object> map = new HashMap<>();
            // 通知模板订阅者
            /*
            final String msg = "基于模板(ID=" + template + ")创建了新的事件，事件ID为" + id;
            map.put("msg", msg);
            map.put("event", id);
            map.put("template", template);
            SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(template, "template");
            if(subscribeMessage != null) {
                System.out.println(subscribeMessage);
                List<String> userList = subscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(JSON.toJSONString(map), user);
                }
            }
            */
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
        if (!redisEventService.deleteEventById(id, template)) {
            return  "删除失败！";
        }
        /*
        List<String> objects = eventObject.getObjects();
        for(String o : objects) {
            Map<String, Object> map1 = new HashMap<>();
            SubscribeMessage entitySubscribeMessage = subscribeService.findByIdAndType(o, "entity");
            if(entitySubscribeMessage != null) {
                String msg1 = "实体对象(ID=" + o + ")关联的事件被删除，事件ID为 " + id;
                map1.put("msg", msg1);
                map1.put("id", o);
                map1.put("event", id);
                map1.put("op", "SUB_RES_EVENT_DELETE");
                List<String> entitySubscribeList = entitySubscribeMessage.getObjectSubscriber();
                for(String user : entitySubscribeList) {
                    subscribeSender.send(JSON.toJSONString(map1), user);
                }
            }
        }

         */
        // 通知模板订阅者
        /*
        SubscribeMessage templateSubscribeMessage = subscribeService.findByIdAndType(id, "template");
        if(templateSubscribeMessage != null) {
            Map<String, Object> map2 = new HashMap<>();
            String msg2 = "基于模板(ID=" + template + ")创建的事件已经被删除，事件ID为" + id;
            map2.put("msg", msg2);
            map2.put("template", template);
            map2.put("event", id);
            List<String> templateSubscriberList = templateSubscribeMessage.getObjectSubscriber();
            for(String user : templateSubscriberList) {
                subscribeSender.send(JSON.toJSONString(map2), user);
            }
        }
        */
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
            SubscribeMessage subscribeMessage;
            String msg;
            List<String> userList;
            /*
            String template = eventObject.getTemplate();
            String msg = "基于模板(ID=" + template + ")创建的事件结束，事件ID为" + id;
            map.put("msg", msg);
            map.put("event", id);
            map.put("template", template);
            SubscribeMessage subscribeMessage = subscribeService.findByIdAndType(template, "template");
            if(subscribeMessage != null) {
                userList = subscribeMessage.getObjectSubscriber();
                for (String user : userList) {
                    subscribeSender.send(JSON.toJSONString(map), user);
                }
            }
            */
            /*
            for(String object : objects) {
                subscribeMessage = subscribeService.findByIdAndType(object, "entity");
                msg = "实体对象(ID=" + object + ")关联的事件结束，事件ID为 " + id;
                userList = subscribeMessage.getObjectSubscriber();
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
}
