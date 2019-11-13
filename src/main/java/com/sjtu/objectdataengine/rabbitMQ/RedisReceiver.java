package com.sjtu.objectdataengine.rabbitMQ;

import com.sjtu.objectdataengine.service.redis.RedisObjectService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RabbitListener(queues = "RedisQueue")//监听的队列名称 MongoQueue
public class RedisReceiver {
    @Autowired
    private RedisObjectService redisObjectService;

    @RabbitHandler
    public void process(Map message) {
        String op = message.get("op").toString();

        /**
         * 每个操作信息都用map描述
         * create表示创建一个新的object
         * create中需要的信息有：
         * op ： CREATE
         * id : 对象id
         * template ： 对象模板id
         * objects ： String列表，表示关联objects（的id）
         * attrs： HashMap类型，属性键值
         */
        if (op.equals("CREATE")) {
            String id = message.get("id").toString();
            String intro = message.get("intro").toString();
            String template = message.get("template").toString();
            List<String> objects = (List<String>) message.get("objects");
            HashMap<String, String> attrs = (HashMap<String, String>) message.get("attrs");
            redisObjectService.create(id, intro, template, objects, attrs);
        }
    }
}
