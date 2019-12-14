package com.sjtu.objectdataengine.rabbitMQ.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    map.put("op", "REGISTER");
                }
                else {
                    map.put("status", "SUCC");
                    map.put("userId", res);
                    map.put("op", "REGISTER");
                }
                subscribeSender.send(JSON.toJSONString(map), res);
                // 客户端怎么知道监听哪条队列？
                break;
            }
            /*
             * 注销用户
             */
            /*
            case "UNREGISTER" : {
                String id = jsonObject.getString("userId");
                boolean res = userService.unregister(id);
                Map<String, Object> map = new HashMap<>();
                if(res) {
                    map.put("status", "SUCC");
                    map.put("op", "REGISTER");
                }
                else {
                    map.put("status", "FAIL");
                    map.put("op", "REGISTER");
                }
                subscribeSender.send(JSON.toJSONString(map), id);
                break;
            }

             */
            /*
             * 订阅请求
             */
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
