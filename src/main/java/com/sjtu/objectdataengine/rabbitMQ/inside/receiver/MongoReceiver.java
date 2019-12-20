package com.sjtu.objectdataengine.rabbitMQ.inside.receiver;

import com.sjtu.objectdataengine.service.event.MongoEventService;
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

    @Resource
    private MongoEventService mongoEventService;

    @RabbitHandler
    public void process(Map message) {
        String op = message.get("op").toString();

        switch (op) {
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
            case "OBJECT_CREATE": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String intro = message.get("intro").toString();
                    String template = message.get("template").toString();
                    List<String> events = TypeConversion.cast(message.get("events"));
                    HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                    Date date = (Date) message.get("date");
                    mongoObjectService.create(id, name, intro, template, attrs, events, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 增加一个属性记录
             */
            case "OBJECT_ADD_ATTR": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String value = message.get("value").toString();
                    Date date = (Date) message.get("date");
                    mongoObjectService.addAttr(id, name, value, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 增加一个关联事件
             */
            case "OBJECT_ADD_EVENT": {
                try {
                    String objId = message.get("objId").toString();
                    String eventId = message.get("eventId").toString();
                    Date date = (Date) message.get("date");
                    mongoObjectService.addEvent(objId, eventId, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 创建知识树节点
             */
            case "NODE_CREATE": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String intro = message.get("intro").toString();
                    String parent = message.get("parent").toString();
                    List<String> children = new ArrayList<>();
                    Date date = (Date) message.get("date");
                    mongoTreeService.createTreeNode(id, name, intro, parent, children, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 删除知识树节点
             */
            case "NODE_DELETE": {
                try {
                    String id = message.get("id").toString();
                    // 传过来的是""或者是数字,没有null
                    String template = message.get("template").toString();
                    Date date = (Date) message.get("date");
                    mongoTreeService.deleteWholeNodeByKey(id, template, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 修改知识树节点
             */
            case "NODE_MODIFY": {
                try {
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
                    Date date = (Date) message.get("date");
                    mongoTreeService.updateNodeByKey(id, name, intro, parent, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }

            /*
             * 创建模板
             */
            case "TEMP_CREATE": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String intro = message.get("intro").toString();
                    String nodeId = message.get("nodeId").toString();
                    String type = message.get("type").toString();
                    HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                    Date date = (Date) message.get("date");
                    mongoTemplateService.createObjectTemplate(id, name, intro, nodeId, type, attrs, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 删除模板
             */
            case "TEMP_DELETE": {
                try {
                    String id = message.get("id").toString();
                    String nodeId = message.get("nodeId").toString();
                    Date date = (Date) message.get("date");
                    mongoTemplateService.deleteTemplateById(id, nodeId, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 修改模板的基础信息
             * 只能修改name和intro
             */
            case "TEMP_MODIFY_BASE": {
                try {
                    String id = message.get("id").toString();
                    String name, intro;
                    if (message.get("name") != null)
                        name = message.get("name").toString();
                    else name = null;
                    if (message.get("intro") != null)
                        intro = message.get("intro").toString();
                    else intro = null;
                    Date date = (Date) message.get("date");
                    mongoTemplateService.updateBaseInfo(id, name, intro, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 模板添加一个属性
             * 包括name和intro
             */
            case "TEMP_ADD_ATTR": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String nickname = message.get("nickname").toString();
                    Date date = (Date) message.get("date");
                    mongoTemplateService.addAttrs(id, name, nickname, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 模板删除一个属性
             * 通过id和name
             */
            case "TEMP_DEL_ATTR": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    Date date = (Date) message.get("date");
                    mongoTemplateService.delAttrs(id, name, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 创建事件
             */
            case "EVENT_CREATE": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String intro = message.get("intro").toString();
                    String template = message.get("template").toString();
                    HashMap<String, String> attrs = TypeConversion.cast(message.get("attrs"));
                    Date date = (Date) message.get("date");
                    mongoEventService.create(id, name, intro, template, attrs, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 删除一个事件
             */
            case "EVENT_DELETE" : {
                try {
                    String id = message.get("id").toString();
                    String template = message.get("template").toString();
                    mongoEventService.deleteEventById(id, template);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 修改事件基础信息
             */
            case "EVENT_MODIFY_BASE": {
                try {
                    String id = message.get("id").toString();
                    String name, intro, stage;
                    if (message.get("name") != null)
                        name = message.get("name").toString();
                    else name = null;
                    if (message.get("intro") != null)
                        intro = message.get("intro").toString();
                    else intro = null;
                    if (message.get("stage") != null)
                        stage = message.get("stage").toString();
                    else stage = null;
                    Date date = (Date) message.get("date");
                    mongoEventService.updateBaseInfo(id, name, intro, stage, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 事件属性修改
             */
            case "EVENT_MODIFY_ATTR": {
                try {
                    String id = message.get("id").toString();
                    String name = message.get("name").toString();
                    String value = message.get("value").toString();
                    Date date = (Date) message.get("date");
                    mongoEventService.modifyAttr(id, name, value, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 事件添加绑定对象
             */
            case "EVENT_ADD_OBJECT": {
                try {
                    String objId = message.get("objId").toString();
                    String eventId = message.get("eventId").toString();
                    mongoEventService.addObject(eventId, objId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            /*
             * 事件结束
             */
            case "EVENT_END" : {
                try {
                    String id = message.get("id").toString();
                    Date date = (Date) message.get("date");
                    mongoEventService.end(id, date);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            default:
                break;
        }
    }
}
