package com.sjtu.objectdataengine.rabbitMQ.outside.receiver;

import com.alibaba.fastjson.JSON;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public void process(String message) {
        JSONObject jsonObject = JSON.parseObject(message);
        // CREATE ADD_ATTR DELETE
        // FIND_ID FIND_TIME FIND_TIMES
        // FIND_EVENT
        String op = jsonObject.getString("op");
        String userId = jsonObject.getString("userId");

        switch (op) {
            case "CREATE": {
                String msg = apiObjectService.create(message);
                if (jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    if (msg.equals("创建成功")) {
                        result.put("status", "SUCC");
                    } else {
                        result.put("status", "FAIL");
                    }
                    result.put("message", msg);
                    subscribeSender.send(result, userId);
                }
<<<<<<< HEAD
                result.put("message", msg);
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "ADD_ATTR": {
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String value = jsonObject.getString("value");
                String msg = apiObjectService.addAttr(id, name, value);
                if (jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    if (msg.equals("添加成功")) {
                        result.put("status", "SUCC");
                    } else {
                        result.put("status", "FAIL");
                    }
                    result.put("message", msg);
                    subscribeSender.send(result, userId);
                }
<<<<<<< HEAD
                result.put("message", msg);
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "DELETE": {
                String id = jsonObject.getString("id");
                String msg = apiObjectService.deleteObjectById(id);
                if (jsonObject.getBoolean("response")) {
                    Map<String, Object> result = new HashMap<>();
                    if (msg.equals("删除成功")) {
                        result.put("status", "SUCC");
                    } else {
                        result.put("status", "FAIL");
                    }
                    result.put("message", msg);
                    subscribeSender.send(result, userId);
                }
<<<<<<< HEAD
                result.put("message", msg);
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "FIND_ID": {
                String id = jsonObject.getString("id");
                CommonObject commonObject = apiObjectService.findObjectById(id);
                Map<String, Object> result = new HashMap<>();
                if (commonObject != null) {
                    result.put("status", "SUCC");
                } else {
                    result.put("status", "FAIL");
                }
                result.put("object", commonObject);
<<<<<<< HEAD
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
                subscribeSender.send(result, userId);
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "FIND_TIME": {
                String id =jsonObject.getString("id");
                Date date = jsonObject.getDate("date");
                CommonObject commonObject = apiObjectService.findObjectByTime(id, date);
                Map<String, Object> result = new HashMap<>();
                if (commonObject != null) {
                    result.put("status", "SUCC");
                } else {
                    result.put("status", "FAIL");
                }
                result.put("object", commonObject);
<<<<<<< HEAD
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
                subscribeSender.send(result, userId);
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "FIND_TIMES": {
                String id = jsonObject.getString("id");
                Date start = jsonObject.getDate("start");
                Date end = jsonObject.getDate("end");
                List<CommonObject> commonObjects = apiObjectService.findObjectsByTimes(id, start, end);
                Map<String, Object> result = new HashMap<>();
                if (commonObjects != null) {
                    result.put("status", "SUCC");
                } else {
                    result.put("status", "FAIL");
                }
                result.put("object", commonObjects);
<<<<<<< HEAD
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
                subscribeSender.send(result, userId);
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
            case "FIND_EVENT": {
                String nodeId = jsonObject.getString("nodeId");
                String eventId = jsonObject.getString("eventId");
                List<CommonObject> commonObjects = apiObjectService.findObjectsByNodeAndEvent(nodeId, eventId);
                Map<String, Object> result = new HashMap<>();
                if (commonObjects != null) {
                    result.put("status", "SUCC");
                } else {
                    result.put("status", "FAIL");
                }
                result.put("object", commonObjects);
<<<<<<< HEAD
                subscribeSender.send(JSON.toJSONString(result), clientId);
=======
                subscribeSender.send(result, userId);
>>>>>>> ec91df4458a091334b9d1b98fb58cf98541ddc01
                break;
            }
        }
    }
}
