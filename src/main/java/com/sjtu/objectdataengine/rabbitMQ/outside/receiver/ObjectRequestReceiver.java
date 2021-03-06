package com.sjtu.objectdataengine.rabbitMQ.outside.receiver;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.client.Channel;
import com.sjtu.objectdataengine.config.Constants;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.event.APIEventService;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;

@Component
public class ObjectRequestReceiver {
    @Resource
    SubscribeSender subscribeSender;

    @Resource
    APIObjectService apiObjectService;

    @Resource
    APIEventService apiEventService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "ObjectRequestQueue"),
                    exchange = @Exchange(value = "RequestExchange"),
                    key = "OBJECT"
            ),
            //errorHandler = "rabbitListenerErrorHandler",
            concurrency = "10"
    )
    public void process(byte[] byteMsg) throws IOException {

        String message = new String(byteMsg);
        System.out.println(message);
        JSONObject jsonObject = JSON.parseObject(message);
        String op = jsonObject.getString("op"); // CREATE ADD_ATTR DELETE FIND_ID FIND_TIME FIND_TIMES FIND_EVENT
        String userId = jsonObject.getString("userId");

        switch (op) {
            case "CREATE_OBJECT": {
                try {
                    String msg = apiObjectService.create(jsonObject);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", jsonObject.getString("id"));
                        if (msg.equals("创建成功")) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", msg);
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    //channel.basicAck(_message.getMessageProperties().getDeliveryTag(),false);
                    break;
                }
            }
            case "DELETE_OBJECT": {
                try {
                    String id = jsonObject.getString("id");
                    String msg = apiObjectService.deleteObjectById(id);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", id);
                        if (msg.equals("删除成功")) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", msg);
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "UPDATE_OBJECT": {
                try {
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String value = jsonObject.getString("value");
                    String msg = apiObjectService.addAttr(id, name, value);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", id);
                        result.put("name", name);
                        if (msg.equals("添加成功")) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", msg);
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            case "OBJECT_FIND_TIME": {
                try {
                    String id = jsonObject.getString("id");
                    Date date = jsonObject.getDate("date");
                    CommonObject commonObject = apiObjectService.findObjectByTime(id, date);
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", "FIND_TIME");
                    result.put("id", id);
                    result.put("date", date);
                    if (commonObject != null) {
                        result.put("status", "SUCC");
                        result.put("message", "查询成功");
                        result.put("object", commonObject);
                    } else {
                        result.put("status", "FAIL");
                        result.put("message", "对象不存在");
                        result.put("object", null);
                    }
                    subscribeSender.send(JSON.toJSONString(result), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "OBJECT_FIND_ID": {
                try {
                    String id = jsonObject.getString("id");
                    CommonObject commonObject = apiObjectService.findObjectById(id);
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", op);
                    result.put("id", id);
                    if (commonObject != null) {
                        result.put("status", "SUCC");
                        result.put("message", "查询成功");
                        result.put("object", commonObject);
                    } else {
                        result.put("status", "FAIL");
                        result.put("message", "对象不存在");
                        result.put("object", null);
                    }
                    subscribeSender.send(JSON.toJSONString(result), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "OBJECT_FIND_TIMES": {
                try {
                    String id = jsonObject.getString("id");
                    Date start = jsonObject.getDate("start");
                    Date end = jsonObject.getDate("end");
                    List<CommonObject> commonObjects = apiObjectService.findObjectsByTimes(id, start, end);
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", op);
                    result.put("id", id);
                    result.put("start", start);
                    result.put("end", end);
                    if (commonObjects != null) {
                        result.put("status", "SUCC");
                        result.put("message", "查询成功");
                    } else {
                        result.put("status", "FAIL");
                        result.put("message", "对象不存在");
                    }
                    result.put("objects", commonObjects);
                    subscribeSender.send(JSON.toJSONString(result), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "OBJECT_FIND_NODE_EVENT": {
                try {
                    String nodeId = jsonObject.getString("nodeId");
                    String eventId = jsonObject.getString("eventId");
                    List<CommonObject> commonObjects = apiObjectService.findObjectsByNodeAndEvent(nodeId, eventId);
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", op);
                    result.put("nodeId", nodeId);
                    result.put("eventId", eventId);
                    if (commonObjects != null) {
                        result.put("status", "SUCC");
                        result.put("message", "查询成功");
                    } else {
                        result.put("status", "FAIL");
                        result.put("message", "查询失败");
                    }
                    result.put("objects", commonObjects);
                    // System.out.println(result);
                    subscribeSender.send(JSON.toJSONString(result), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }

            }
            case "CREATE_EVENT": {
                try {
                    ResultInterface res = apiEventService.create(jsonObject);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", jsonObject.getString("id"));
                        if (res.getCode().equals(Constants.EVENT_CREATE_SUCCESS)) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", res.getMsg());
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "DELETE_EVENT": {
                try {
                    String id = jsonObject.getString("id");
                    ResultInterface res = apiEventService.delete(id);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", id);
                        if (res.getCode().equals(Constants.EVENT_DEL_SUCCESS)) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", res.getMsg());
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "UPDATE_EVENT": {
                try {
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    ResultInterface res = apiEventService.modifyAttr(jsonObject);
                    if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                        Map<String, Object> result = new HashMap<>();
                        result.put("op", op);
                        result.put("id", id);
                        result.put("name", name);
                        if (res.getCode().equals(Constants.EVENT_MODIFY_SUCCESS)) {
                            result.put("status", "SUCC");
                        } else {
                            result.put("status", "FAIL");
                        }
                        result.put("message", res.getMsg());
                        subscribeSender.send(JSON.toJSONString(result), userId);
                    }
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
            case "EVENT_FIND_ID": {
                try {
                    String id = jsonObject.getString("id");
                    EventObject eventObject = apiEventService.find(id);
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", op);
                    result.put("id", id);
                    if (eventObject != null) {
                        result.put("status", "SUCC");
                        result.put("message", "查询成功");
                        result.put("event", eventObject);
                    } else {
                        result.put("status", "FAIL");
                        result.put("message", "事件不存在");
                        result.put("event", null);
                    }
                    subscribeSender.send(JSON.toJSONString(result), userId);
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    }
}
