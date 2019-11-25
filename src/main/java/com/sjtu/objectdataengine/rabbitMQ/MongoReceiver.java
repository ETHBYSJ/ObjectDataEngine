package com.sjtu.objectdataengine.rabbitMQ;

import com.sjtu.objectdataengine.service.object.MongoObjectService;
import com.sjtu.objectdataengine.service.template.MongoTemplateService;
import com.sjtu.objectdataengine.service.tree.MongoTreeService;
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

    @Resource
    private MongoTemplateService mongoTemplateService;

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
            case "CREATE": {
                String id = message.get("id").toString();
                String intro = message.get("intro").toString();
                String template = message.get("template").toString();
                List<String> objects = TypeConversion.cast(message.get("objects"));
                HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                mongoObjectService.create(id, intro, template, attrs, objects);
                break;
            }


            /*
             * 创建知识树节点
             */
            case "NODE_CREATE": {
                String id = message.get("id").toString();
                String name = message.get("name").toString();
                String intro = message.get("intro").toString();
                String parent = message.get("parent").toString();
                List<String> children = new ArrayList<>();
                mongoTreeService.createTreeNode(id, name, intro, parent, children);
                break;
            }

            /*
             * 删除知识树节点
             */
            case "NODE_DELETE": {
                String id = message.get("id").toString();
                // 传过来的是""或者是数字,没有null
                String template = message.get("template").toString();
                mongoTreeService.deleteWholeNodeByKey(id, template);
                break;
            }

            /*
             * 修改知识树节点
             */
            case "NODE_MODIFY": {
                String id = message.get("id").toString();
                String name, intro, parent;
                if (message.get("name") != null)
                    name = message.get("name").toString();
                else name = null;
                if (message.get("intro") != null)
                    intro = message.get("intro").toString();
                else intro = null;
                if (message.get("parents") != null)
                    parent = message.get("parent").toString();
                else parent = null;
                mongoTreeService.updateNodeByKey(id, name, intro, parent);
                break;
            }

            /*
             * 创建模板
             */
            case "TEMP_CREATE": {
                String id = message.get("id").toString();
                String name = message.get("name").toString();
                String intro = message.get("intro").toString();
                String nodeId = message.get("nodeId").toString();
                String type = message.get("type").toString();
                HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                mongoTemplateService.createObjectTemplate(id, name, intro, nodeId, type, attrs);
                break;
            }
            case "TEMP_DELETE": {
                String id = message.get("id").toString();
                String nodeId = message.get("nodeId").toString();
                mongoTemplateService.deleteTemplateById(id, nodeId);
                break;
            }

            /*
             * 修改模板的基础信息
             * 只能修改name和intro
             */
            case "TEMP_MODIFY_BASE": {
                String id = message.get("id").toString();
                String name, intro, type;
                if (message.get("name") != null)
                    name = message.get("name").toString();
                else name = null;
                if (message.get("intro") != null)
                    intro = message.get("intro").toString();
                else intro = null;
                mongoTemplateService.updateBaseInfo(id, name, intro);
                break;
            }

            case "TEMP_ADD_ATTR": {

                break;
            }
        }
    }
}
