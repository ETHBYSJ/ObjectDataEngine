package com.sjtu.objectdataengine.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class EventService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisEventService redisEventService;

    @Resource
    RedisTemplateService redisTemplateService;

    @Resource
    MongoEventService mongoEventService;

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
        //else if (!redisTemplateService.hasKey(template)) return "template不存在";
        else if(!redisTemplateService.hasTemplate(template)) return "template不存在";
        JSONObject attrObject = jsonObject.getJSONObject("attrs");
        HashMap<String, String> attrs = new HashMap<>();
        if (attrObject != null) {
            for (Map.Entry entry : attrObject.entrySet()) {
                String key = entry.getKey().toString();
                String value = entry.getValue().toString();
                attrs.put(key, value);
            }
        }
        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "EVENT_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("intro", intro);
        message.put("template", template);
        message.put("attrs", attrs);

        if (redisEventService.create(id, name, intro, template, attrs)) {
            mongoSender.send(message);
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
        message.put("id", id);
        message.put("template", template);

        mongoSender.send(message);
        if (redisEventService.deleteEventById(id, template)) {
            return  "删除成功！";
        }
        return "删除失败！";
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
        String intro = jsonObject.getString("name");
        if (intro != null) {
            modifyMessage.put("intro", intro);
        }
        // stage如果是null就不需要改
        String stage = jsonObject.getString("stage");
        if (stage != null) {
            modifyMessage.put("stage", stage);
        }

        mongoSender.send(modifyMessage);
        if (redisEventService.updateBaseInfo(id, name, intro, stage)) {
            return "修改成功";
        }
        return "修改失败";
    }

    public String end(String id) {
        if (id == null || id.equals("")) return "ID不能为空";

        EventObject eventObject = redisEventService.findEventObjectById(id);
        if (eventObject == null) return "没有该事件或事件已经结束";

        HashMap<String, Object> endMessage = new HashMap<>();

        endMessage.put("op", "EVENT_END");
        endMessage.put("id", id);

        // 删除redis中的已结束事件
        if (redisEventService.deleteEventById(id, eventObject.getTemplate())) {
            mongoSender.send(endMessage);
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
}
