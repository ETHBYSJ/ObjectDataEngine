package com.sjtu.objectdataengine.rabbitMQ.receiver;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@RabbitListener(queues = "SubscribeRequestQueue")
public class SubscribeRequestReceiver {
    //监听订阅相关消息

    @Resource
    private SubscribeService subscribeService;

    @Resource
    private UserService userService;

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
                userService.register(name, intro);
                // 客户端怎么知道监听哪条队列？
                break;
            }
            /*
             * 注销用户
             */
            case "UNREGISTER" : {
                String id = jsonObject.getString("id");
                userService.unregister(id);
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
                if(name == null) {
                    subscribeService.addObjectSubscriber(objId, type, userId);
                }
                else {
                    subscribeService.addAttrSubscriber(objId, type, name.toString(), userId);
                }
                break;
            }
        }
    }
}
