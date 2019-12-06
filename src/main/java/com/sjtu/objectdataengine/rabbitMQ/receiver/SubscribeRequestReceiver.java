package com.sjtu.objectdataengine.rabbitMQ.receiver;

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
    public void process(Map message) {
        String op = message.get("op").toString();
        switch(op) {
            /*
             * 注册用户
             */
            case "REGISTER" : {
                String name = message.get("name").toString();
                String intro = message.get("intro").toString();
                userService.register(name, intro);
                // 客户端怎么知道监听哪条队列？
                break;
            }
            /*
             * 注销用户
             */
            case "UNREGISTER" : {
                String id = message.get("id").toString();
                userService.unregister(id);
                break;
            }
            /*
             * 订阅请求
             */
            case "SUB" : {
                String userId = message.get("id").toString();
                String objId = message.get("obj").toString();
                String type = message.get("type").toString();
                String name = message.get("name") != null ? message.get("name").toString() : null;
                userService.addObjectSubscribe(userId, type, objId, name);
                break;
            }
        }
    }
}
