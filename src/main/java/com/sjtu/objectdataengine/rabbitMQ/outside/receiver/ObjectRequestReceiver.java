package com.sjtu.objectdataengine.rabbitMQ.outside.receiver;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class ObjectRequestReceiver {
    @Resource
    SubscribeSender subscribeSender;

    @Resource
    APIObjectService apiObjectService;

    @RabbitListener(
            bindings = @QueueBinding(
                    value = @Queue(value = "ObjectRequestQueue"),
                    exchange = @Exchange(value = "RequestExchange"),
                    key = "OBJECT"
            ),
            concurrency = "10"
    )
    public void process(byte[] byteMsg) {
        String message = new String(byteMsg);
        // System.out.println("get message!!!");
        System.out.println(message);
        JSONObject jsonObject = JSON.parseObject(message);
        String op = jsonObject.getString("op"); // CREATE ADD_ATTR DELETE FIND_ID FIND_TIME FIND_TIMES FIND_EVENT
        String userId = jsonObject.getString("userId");

        switch (op) {
            case "CREATE": {
                String msg = apiObjectService.create(jsonObject);
                if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", "CREATE");
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
            }
            case "DELETE": {
                String id = jsonObject.getString("id");
                String msg = apiObjectService.deleteObjectById(id);
                if (jsonObject.getBoolean("response") != null && jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", "DELETE");
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
            }
            case "UPDATE": {
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String value = jsonObject.getString("value");
                String msg = apiObjectService.addAttr(id, name, value);
                if (jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("op", "UPDATE");
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
                break;
            }
            case "FIND_ID": {
                String id = jsonObject.getString("id");
                CommonObject commonObject = apiObjectService.findObjectById(id);
                // System.out.println(commonObject);
                Map<String, Object> result = new HashMap<>();
                result.put("op", "FIND_ID");
                result.put("id", id);
                if (commonObject != null) {
                    result.put("status", "SUCC");
                    result.put("object", commonObject.toString());
                    // System.out.println(commonObject.toString());
                } else {
                    result.put("status", "FAIL");
                    result.put("object", null);
                }
                result.put("object", commonObject);
                subscribeSender.send(JSON.toJSONString(result), userId);
                break;
            }
            case "FIND_TIME": {
                String id =jsonObject.getString("id");
                Date date = jsonObject.getDate("date");
                CommonObject commonObject = apiObjectService.findObjectByTime(id, date);
                Map<String, Object> result = new HashMap<>();
                result.put("op", "FIND_TIME");
                result.put("id", id);
                result.put("date", date);
                if (commonObject != null) {
                    result.put("status", "SUCC");
                    result.put("object", commonObject.toString());
                } else {
                    result.put("status", "FAIL");
                    result.put("object", null);
                }
                result.put("object", commonObject);
                subscribeSender.send(JSON.toJSONString(result), userId);
                break;
            }
            case "FIND_TIMES": {
                String id = jsonObject.getString("id");
                Date start = jsonObject.getDate("start");
                Date end = jsonObject.getDate("end");
                List<CommonObject> commonObjects = apiObjectService.findObjectsByTimes(id, start, end);
                Map<String, Object> result = new HashMap<>();
                List<String> objectsJson = new ArrayList<>();
                // JSONArray objectsJson = new JSONArray();
                result.put("op", "FIND_TIMES");
                result.put("id", id);
                result.put("start", start);
                result.put("end", end);
                if (commonObjects != null) {
                    result.put("status", "SUCC");
                    commonObjects.forEach((item)->{
                        objectsJson.add(item.toString());
                    });
                } else {
                    result.put("status", "FAIL");
                }
                result.put("objects", objectsJson);
                subscribeSender.send(JSON.toJSONString(result), userId);
                break;
            }
            case "FIND_EVENT": {
                String nodeId = jsonObject.getString("nodeId");
                String eventId = jsonObject.getString("eventId");
                List<CommonObject> commonObjects = apiObjectService.findObjectsByNodeAndEvent(nodeId, eventId);
                Map<String, Object> result = new HashMap<>();
                List<String> objectsJson = new ArrayList<>();
                result.put("op", "FIND_EVENT");
                result.put("nodeId", nodeId);
                result.put("eventId", eventId);
                if (commonObjects != null) {
                    result.put("status", "SUCC");
                    commonObjects.forEach((item)->{
                        objectsJson.add(item.toString());
                    });
                } else {
                    result.put("status", "FAIL");
                }
                result.put("objects", objectsJson);
                subscribeSender.send(JSON.toJSONString(result), userId);
                break;
            }
        }
    }
}
