package com.sjtu.objectdataengine.rabbitMQ.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
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
                }
                else {
                    map.put("status", "SUCC");
                    map.put("id", res);
                }
                subscribeSender.send(JSON.toJSONString(map), res);
                // 客户端怎么知道监听哪条队列？
                break;
            }
            /*
             * 注销用户
             */
            case "UNREGISTER" : {
                String id = jsonObject.getString("id");
                boolean res = userService.unregister(id);
                Map<String, Object> map = new HashMap<>();
                if(res) {
                    map.put("status", "SUCC");
                }
                else {
                    map.put("status", "FAIL");
                }
                subscribeSender.send(JSON.toJSONString(map), id);
                break;
            }
            /*
             * 订阅请求
             */
            case "SUB" : {
                String userId = jsonObject.getString("id");
                String objId = jsonObject.getString("obj");
                String type = jsonObject.getString("type");
                Object name = jsonObject.getString("name");
                /*
                if(subscribeService.findByIdAndType(objId, type) == null) {
                    subscribeService.create(objId, type);
                }
                */
                String res;
                if(name == null) {
                    res = subscribeService.addObjectSubscriber(objId, type, userId);
                }
                else {
                    res = subscribeService.addAttrSubscriber(objId, type, name.toString(), userId);
                }
                Map<String, Object> map = new HashMap<>();
                if(res.equals("增加成功")) {
                    map.put("status", "SUCC");
                }
                else {
                    map.put("status", "FAIL");
                }
                subscribeSender.send(JSON.toJSONString(map), userId);
                break;
            }
        }
    }
}
