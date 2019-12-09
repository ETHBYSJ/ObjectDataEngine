package com.sjtu.objectdataengine.rabbitMQ.inside.receiver;

import com.sjtu.objectdataengine.service.object.RedisObjectService;
import com.sjtu.objectdataengine.utils.TypeConversion;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RabbitListener(queues = "RedisQueue")//监听的队列名称 MongoQueue
public class RedisReceiver {
    @Resource
    private RedisObjectService redisObjectService;

    @RabbitHandler
    public void process(Map message) {
        String op = message.get("op").toString();

        /*
         * 每个操作信息都用map描述
         * create表示创建一个新的object
         * create中需要的信息有：
         * op ： CREATE
         * id : 对象id
         * template ： 对象模板id
         * objects ： String列表，表示关联objects（的id）
         * attrs： HashMap类型，属性键值
         */
        switch (op) {

            case "OBJECT_CREATE": {
                String id = message.get("id").toString();
                String name = message.get("name").toString();
                String intro = message.get("intro").toString();
                String template = message.get("template").toString();
                List<String> objects = TypeConversion.cast(message.get("events"));
                HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                Date date = (Date) message.get("date");
                redisObjectService.create(id, name, intro, template, objects, attrs, date);
                break;
            }

            case "EVICT_AND_ADD": {
                String id = message.get("id").toString();
                String name = message.get("name").toString();
                String key = id + '#' + name + '#' + "time";
                //String key = message.get("key").toString();
                String value = message.get("value").toString();
                Date date = (Date) message.get("date");
                redisObjectService.doEvict(id, name);
                redisObjectService.addAttr(key, value, date);
                break;
            }

            case "OBJECT_ADD_ATTR": {
                String id = message.get("id").toString();
                String name = message.get("name").toString();
                String key = id + '#' + name + '#' + "time";
                String value = message.get("value").toString();
                Date date = (Date) message.get("date");
                redisObjectService.addAttr(key, value, date);
                break;
            }
        }
    }
}
