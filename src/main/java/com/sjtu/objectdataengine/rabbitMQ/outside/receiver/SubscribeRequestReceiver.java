package com.sjtu.objectdataengine.rabbitMQ.outside.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import com.sjtu.objectdataengine.utils.Result.ResultData;
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
    public void process(byte[] byteMsg) {
        String message = new String(byteMsg);
        JSONObject jsonObject = JSON.parseObject(message);
        System.out.println(message);
        String op = jsonObject.getString("op");
        switch(op) {
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
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
            // 属性订阅
            case "SUB_ATTR" : {
                String userId = jsonObject.getString("userId");
                String id = jsonObject.getString("id");
                JSONArray jsonArray = jsonObject.getJSONArray("names");
                List<String> attrs = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                boolean latest = jsonObject.getBoolean("latest");
                Map<String, Object> map = new HashMap<String, Object>();
                String res = subscribeService.addEntitySubscriber(id, userId, attrs);
                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                    map.put("message", res);
                    map.put("id", id);
                    map.put("names", attrs);
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
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
            // 模板订阅
            case "SUB_TEMPLATE" : {
                String userId = jsonObject.getString("userId");
                String template = jsonObject.getString("template");
                JSONArray jsonArray = jsonObject.getJSONArray("events");
                List<String> events = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                ObjectTemplate objectTemplate = ((ResultData<ObjectTemplate>)templateService.get(template)).getData();
                Map<String, Object> map = new HashMap<>();
                if(objectTemplate.getType().equals("entity")) {
                    // 检查事件列表
                } else {
                    events = new ArrayList<>();
                }

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
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }

        }
    }
}
