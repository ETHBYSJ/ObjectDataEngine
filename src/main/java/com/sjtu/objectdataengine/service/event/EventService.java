package com.sjtu.objectdataengine.service.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

public class EventService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisEventService redisEventService;

    @Resource
    RedisTemplateService redisTemplateService;

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
        else if (!redisTemplateService.hasKey(template)) return "template不存在";

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
        message.put("op", "TEMP_DELETE");
        message.put("id", id);
        message.put("template", template);

        mongoSender.send(message);
        if (redisTemplateService.deleteEventById(id)) {
            return  "删除成功！";
        }
        return "删除失败！";
    }
}
