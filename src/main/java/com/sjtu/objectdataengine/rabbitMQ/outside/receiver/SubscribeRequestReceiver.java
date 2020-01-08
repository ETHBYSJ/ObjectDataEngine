package com.sjtu.objectdataengine.rabbitMQ.outside.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.object.MongoAttr;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import com.sjtu.objectdataengine.config.Constants;

import javax.annotation.Resource;
import java.util.*;

@Component
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

    @RabbitListener(queues = "SubscribeRequestQueue")
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
                try {
                    String userId = jsonObject.getString("userId");
                    String id = jsonObject.getString("id");
                    boolean latest = jsonObject.getBoolean("latest");
                    Map<String, Object> map = new HashMap<String, Object>();
                    ResultInterface res = subscribeService.addEntitySubscriber(id, userId, null);
                    if(res.getCode().equals(Constants.SUB_ADD_ENTITY_SUCCESS)) {
                        map.put("status", "SUCC");
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("op", op);
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
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("object", null);
                        map.put("op", op);
                    }
                    subscribeSender.send(JSON.toJSONString(map), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            // 属性订阅
            case "SUB_ATTR" : {
                try {
                    String userId = jsonObject.getString("userId");
                    String id = jsonObject.getString("id");
                    JSONArray jsonArray = jsonObject.getJSONArray("names");
                    List<String> attrs = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                    boolean latest = jsonObject.getBoolean("latest");
                    Map<String, Object> map = new HashMap<String, Object>();
                    ResultInterface res = subscribeService.addEntitySubscriber(id, userId, attrs);
                    if(res.getCode().equals(Constants.SUB_ADD_ENTITY_SUCCESS)) {
                        map.put("status", "SUCC");
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("names", attrs);
                        map.put("op", op);
                        if(latest) {
                            Date date = new Date();
                            Map<String, MongoAttr> retMap = new HashMap<>();
                            for(String attr : attrs) {
                                retMap.put(attr, objectService.findAttrByTime(id, attr, date));
                            }
                            map.put("attrs", retMap);
                        }
                        else {
                            map.put("attrs", null);
                        }
                    }
                    else {
                        map.put("status", "FAIL");
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("name", attrs);
                        map.put("attrs", null);
                        map.put("op", op);
                    }
                    subscribeSender.send(JSON.toJSONString(map), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            // 模板订阅
            case "SUB_TEMPLATE" : {
                try {
                    String userId = jsonObject.getString("userId");
                    String template = jsonObject.getString("template");
                    JSONArray jsonArray = jsonObject.getJSONArray("events");
                    List<String> events = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                    ObjectTemplate objectTemplate = templateService.getTemplateById(template);
                    Map<String, Object> map = new HashMap<>();
                    if(objectTemplate == null) {
                        map.put("status", "FAIL");
                        map.put("template", template);
                        map.put("events", events);
                        map.put("message", "模板不存在");
                        map.put("op", op);
                    }
                    else {
                        if(objectTemplate.getType().equals("entity")) {
                            // 检查事件列表
                        } else {
                            events = new ArrayList<>();
                        }
                        ResultInterface res = subscribeService.addTemplateSubscriber(template, userId, events);
                        if(res.getCode().equals(Constants.SUB_ADD_TEMPLATE_SUCCESS)) {
                            map.put("status", "SUCC");
                            map.put("template", template);
                            map.put("events", events);
                            map.put("message", res.getMsg());
                            map.put("op", op);
                        }
                        else {
                            map.put("status", "FAIL");
                            map.put("template", template);
                            map.put("events", events);
                            map.put("message", res.getMsg());
                            map.put("op", op);
                        }
                        subscribeSender.send(JSON.toJSONString(map), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "UN_SUB_OBJECT" : {
                try {
                    String userId = jsonObject.getString("userId");
                    String id = jsonObject.getString("id");
                    Map<String, Object> map = new HashMap<String, Object>();
                    ResultInterface res = subscribeService.delEntitySubscriber(id, userId, null);
                    if(res.getCode().equals(Constants.SUB_DEL_ENTITY_SUCCESS)) {
                        map.put("status", "SUCC");
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("op", op);
                    }
                    else {
                        map.put("status", "FAIL");
                        map.put("message", res.getMsg());
                        map.put("id", id);
                        map.put("op", op);
                    }
                    subscribeSender.send(JSON.toJSONString(map), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "UNSUB_OBJECT" : {
                String userId = jsonObject.getString("userId");
                String id = jsonObject.getString("id");
                Map<String, Object> map = new HashMap<String, Object>();
                ResultInterface res = subscribeService.delEntitySubscriber(id, userId, null);
                if(res.getCode().equals(Constants.SUB_DEL_ENTITY_SUCCESS)) {
                    map.put("status", "SUCC");
                    map.put("message", res.getMsg());
                    map.put("id", id);
                    map.put("op", op);
                }
                else {
                    map.put("status", "FAIL");
                    map.put("message", res.getMsg());
                    map.put("id", id);
                    map.put("op", op);
                }
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
            case "UNSUB_ATTR" : {
                String userId = jsonObject.getString("userId");
                String id = jsonObject.getString("id");
                JSONArray jsonArray = jsonObject.getJSONArray("names");
                List<String> attrs = jsonArray == null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
                Map<String, Object> map = new HashMap<String, Object>();
                ResultInterface res = subscribeService.delEntitySubscriber(id, userId, attrs);
                if(res.getCode().equals(Constants.SUB_DEL_ENTITY_SUCCESS)) {
                    map.put("status", "SUCC");
                    map.put("message", res.getMsg());
                    map.put("id", id);
                    map.put("op", op);
                }
                else {
                    map.put("status", "FAIL");
                    map.put("message", res.getMsg());
                    map.put("id", id);
                    map.put("op", op);
                }
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
            case "UN_SUB_TEMPLATE" : {
                try {
                    String userId = jsonObject.getString("userId");
                    String template = jsonObject.getString("template");
                    ObjectTemplate objectTemplate = templateService.getTemplateById(template);
                    Map<String, Object> map = new HashMap<>();
                    if(objectTemplate == null) {
                        map.put("status", "FAIL");
                        map.put("template", template);
                        map.put("message", "模板不存在");
                        map.put("op", op);
                    }
                    else {
                        ResultInterface res = subscribeService.delTemplateSubscriber(template, userId);
                        if(res.getCode().equals(Constants.SUB_DEL_TEMPLATE_SUCCESS)) {
                            map.put("status", "SUCC");
                            map.put("template", template);
                            map.put("message", res.getMsg());
                            map.put("op", op);
                        }
                        else {
                            map.put("status", "FAIL");
                            map.put("template", template);
                            map.put("message", res.getMsg());
                            map.put("op", op);
                        }
                    }
                    subscribeSender.send(JSON.toJSONString(map), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
