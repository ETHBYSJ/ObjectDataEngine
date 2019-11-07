package com.sjtu.objectdataengine.rabbitMQ;

import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.service.MongoObjectService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RabbitListener(queues = "MongoQueue")//监听的队列名称 MongoQueue
public class MongoReceiver {

    @Autowired
    private MongoObjectService mongoObjectService;

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
        if(op.equals("CREATE")) {
            String id = message.get("id").toString();
            String intro = message.get("intro").toString();
            String template = message.get("template").toString();
            List<String> objects = (List<String>) message.get("objects");
            HashMap<String, String> attrs = (HashMap<String, String>) message.get("attrs");
            mongoObjectService.create(id, intro, template, attrs, objects);
        }


        /**
         * find_key表示，根据object的id查询到最新的object状态
         * find_key中需要的信息有：
         * op ： FIND_KEY
         * id ： 对象id（即key）
         */
        else if (op.equals("FIND_KEY")) {
            String id = message.get("id").toString();
            mongoObjectService.findLatestObjectByKey(id);
        }

        /**
         * find_time表示，根据时间和object的id查询此对象对应时间的状态
         * find_time中需要的信息有：
         * op ： FIND_TIME
         * id ： 对象id
         * time ： 查询时间
         */
        else if (op.equals("FIND_TIME")) {
            String id = message.get("id").toString();
            Date time = (Date) message.get("time");
            mongoObjectService.findObjectByTime(id, time);
        }

        /**
         * find_times表示，根据时间和object的id查询此对象对应时间段的状态变化
         * find_time中需要的信息有：
         * op ： FIND_TIMES
         * id ： 对象id
         * start ： 开始时间
         * end ： 结束时间
         */
        else if (op.equals("FIND_TIMES")) {
            String id = message.get("id").toString();
            Date start = (Date) message.get("start");
            Date end = (Date) message.get("end");
            mongoObjectService.findObjectByStartAndEnd(id, start, end);
        }

        /**
         *
         */
        else if (op.equals("FIND_ATTR_KEY")) {

        }

        else if (op.equals("FIND_ATTR_TIME")) {

        }

        else if (op.equals("FIND_ATTR_TIMES")) {

        }

        else if (op.equals("UPDATE")) {

        }

        else if (op.equals("DELETE")) {

        }

        else {

        }
    }
}
