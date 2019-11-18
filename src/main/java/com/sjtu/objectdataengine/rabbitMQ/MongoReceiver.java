package com.sjtu.objectdataengine.rabbitMQ;

import com.sjtu.objectdataengine.service.mongodb.MongoObjectService;
import com.sjtu.objectdataengine.service.mongodb.MongoTreeService;
import com.sjtu.objectdataengine.utils.TypeConversion;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
@RabbitListener(queues = "MongoQueue")//监听的队列名称 MongoQueue
public class MongoReceiver {

    @Resource
    private MongoObjectService mongoObjectService;

    @Resource
    private MongoTreeService mongoTreeService;

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
            List<String> objects = TypeConversion.cast(message.get("objects"));
            HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
            mongoObjectService.create(id, intro, template, attrs, objects);
        }


        /**
         * 创建知识树节点
         */
        else if (op.equals("NODE_CREATE")) {
            String id = message.get("id").toString();
            String name = message.get("name").toString();
            String template = message.get("template").toString();
            List<String> parents =  TypeConversion.cast(message.get("parents"));
            List<String> children = new ArrayList<>();
            mongoTreeService.createTreeNode(id, name, template, parents, children);
        }

        /**
         * 删除知识树节点
         */
        else if (op.equals("NODE_DELETE")) {
            String id = message.get("id").toString();
            mongoTreeService.deleteWholeNodeByKey(id);
        }

        else if (op.equals("NODE_MODIFY")) {
            String id = message.get("id").toString();
            String name, template;
            List<String> parents;
            if (message.get("name") != null)
                name = message.get("name").toString();
            else name = null;
            if (message.get("template") != null)
                template = message.get("template").toString();
            else template = null;
            if (message.get("parents") != null)
                parents =  TypeConversion.cast(message.get("parents"));
            else parents = null;
            mongoTreeService.updateNodeByKey(id, name, template, parents);
        }

        else if (op.equals("UPDATE")) {

        }

        else if (op.equals("DELETE")) {

        }

        else {

        }
    }
}
