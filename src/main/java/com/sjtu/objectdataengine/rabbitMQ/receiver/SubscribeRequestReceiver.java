package com.sjtu.objectdataengine.rabbitMQ.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.Mongo;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@RabbitListener(queues = "SubscribeRequestQueue")
public class SubscribeRequestReceiver {
    //监听订阅相关消息

    @Resource
    private SubscribeService subscribeService;

    @Resource
    private UserService userService;

    @Resource
    private SubscribeSender subscribeSender;

    @Resource
    private APITemplateService templateService;

    @Resource
    private APIObjectService objectService;

    @RabbitHandler
    public void process(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        String op = jsonObject.getString("op");
        switch(op) {
            /*
             * 注册用户
             */
            case "REGISTER" : {
                String name = jsonObject.getString("name");
                String intro = jsonObject.getString("intro");
                String res = userService.register(name, intro);
                Map<String, Object> map = new HashMap<>();
                if(res.equals("用户名重复")) {
                    map.put("status", "FAIL");
                    map.put("message", res);
                    map.put("op", "REGISTER");
                }
                else {
                    map.put("status", "SUCC");
                    map.put("message", "注册成功");
                    map.put("userId", res);
                    map.put("name", name);
                    map.put("intro", intro);
                    map.put("op", "REGISTER");
                }
                subscribeSender.send(JSON.toJSONString(map), res);
                // 客户端怎么知道监听哪条队列？
                break;
            }
            /*
             * 注销用户
             */

            case "UNREGISTER" : {
                String userId = jsonObject.getString("userId");
                boolean res = userService.unregister(userId);
                Map<String, Object> map = new HashMap<>();
                if(res) {
                    map.put("status", "SUCC");
                    map.put("op", "REGISTER");
                }
                else {
                    map.put("status", "FAIL");
                    map.put("op", "REGISTER");
                }
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }

            /*
             * 订阅请求
             */
            // 对象订阅
            case "SUB_OBJECT" : {
                String userId = jsonObject.getString("userId");
                String id = jsonObject.getString("id");
                boolean latest = jsonObject.getBoolean("latest");
                Map<String, Object> map = new HashMap<String, Object>();
                String res = subscribeService.addEntitySubscriber(id, userId, null);
                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                    map.put("message", res);
                    map.put("id", id);
                    if(latest) {
                        CommonObject commonObject = objectService.findObjectById(id);
                        map.put("object", commonObject);
                    }
                    else {
                        map.put("object", null);
                    }
                }
                else {
                    map.put("status", "FAIL");
                    map.put("message", res);
                    map.put("id", id);
                    map.put("object", null);
                }

            }
            // 属性订阅
            case "SUB_ATTR" : {
                String userId = jsonObject.getString("userId");
                String id = jsonObject.getString("id");
                JSONArray jsonArray = jsonObject.getJSONArray("name");
                List<String> attrs = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                boolean latest = jsonObject.getBoolean("latest");
                Map<String, Object> map = new HashMap<String, Object>();
                String res = subscribeService.addEntitySubscriber(id, userId, attrs);
                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                    map.put("message", res);
                    map.put("id", id);
                    map.put("name", attrs);
                    if(latest) {
                        Date date = new Date();
                        Map<String, String> retMap = new HashMap<>();
                        for(String attr : attrs) {
                            retMap.put(attr, objectService.findAttrByTime(id, attr, date).getValue());
                        }
                        map.put("attrs", retMap);
                    }
                    else {
                        map.put("attrs", null);
                    }
                }
                else {
                    map.put("status", "FAIL");
                    map.put("message", res);
                    map.put("id", id);
                    map.put("name", attrs);
                    map.put("attrs", null);
                }
                break;
            }
            // 模板订阅
            case "SUB_TEMPLATE" : {
                String userId = jsonObject.getString("userId");
                String template = jsonObject.getString("template");
                JSONArray jsonArray = jsonObject.getJSONArray("events");
                List<String> events = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                ObjectTemplate objectTemplate = templateService.get(template);
                if(objectTemplate.getType().equals("entity")) {
                    // 检查事件列表
                }
                Map<String, Object> map = new HashMap<>();
                String res = subscribeService.addTemplateSubscriber(template, userId, events);
                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                    map.put("template", template);
                    map.put("events", events);
                    map.put("message", res);
                }
                else {
                    map.put("status", "FAIL");
                    map.put("template", template);
                    map.put("events", events);
                    map.put("message", res);
                }
            }

            /*
            case "SUB" : {
                String userId = jsonObject.getString("id");
                String objId = jsonObject.getString("obj");
                String type = jsonObject.getString("type");
                String res;
                Map<String, Object> map = new HashMap<>();
                boolean latest = false;
                // 模板订阅，需要传入event列表
                // 如果是事件模板 传入需要监听的事件列表
                // 如果是实体对象模板 传入对象列表，监听与这些对象关联的事件
                if(type.equals("event_template")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("events");
                    List<String> events = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                    // 检查事件有效性
                }
                else if(type.equals("entity_template")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("objects");
                    List<String> objects = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                    // 检查对象有效性
                }
                else if(type.equals("entity")) {
                    JSONArray jsonArray = jsonObject.getJSONArray("attrs");
                    List<String> attrs = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                    if(attrs.size() == 0) {
                        // 表示订阅整个实体对象
                    }
                    else {
                        // 表示订阅部分属性
                        res = subscribeService.addAttrSubscriber(objId, type, )
                    }
                    // 是否立即返回最新一条数据
                    latest = jsonObject.getBoolean("latest");

                }
                else {
                    // 类型错误
                    map.put("status", "FAIL");
                    map.put("op", "REGISTER");
                    map.put("ERR", "WRONG_TYPE");
                }

                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                    map.put("op", "REGISTER");
                }
                else {
                    map.put("status", "FAIL");
                    map.put("op", "REGISTER");
                }
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
            */

        }
    }
}
