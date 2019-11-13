package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.rabbitMQ.RedisSender;
import com.sjtu.objectdataengine.service.mongodb.MongoTreeService;
import com.sjtu.objectdataengine.service.redis.RedisTreeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TreeService {
    @Resource
    private MongoSender mongoSender;

    @Resource
    private RedisSender redisSender;

    @Resource
    private MongoTreeService mongoTreeService;

    @Resource
    private RedisTreeService redisTreeService;

    /**
     * 需要注意create之后，没有template的情况，反之亦然
     * 参数json(*为必须)
     * {
     *     id       String*
     *     name     String
     *     template String
     *     parents  String*
     * }
     */
    public String create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        String name = jsonObject.getString("name");
        if (name == null) name = "";
        String template = jsonObject.getString("template");
        if (template == null) template = "";
        String parents = jsonObject.getString("parents");
        if (parents == null) return "父节点不能为空";

        //要判断是否有空的
        List<String> parentsArray = new ArrayList<>();
        parentsArray.add(parents);
        List<String> children = new ArrayList<>();
        HashMap<String, String> objects = new HashMap<String, String>();

        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("template", template);
        message.put("parents", parentsArray);

        // 双写
        mongoSender.send(message);
        if(redisTreeService.createTreeNode(id, name, template, parentsArray, children, objects))
           return "创建成功！";

        // 若redis失败，则删掉mongodb的
        HashMap<String, Object> deleteMessage = new HashMap<>();
        deleteMessage.put("op", "NODE_DELETE");
        deleteMessage.put("id", id);
        mongoSender.send(deleteMessage);
        return "创建失败!";
    }

    public String delete(String id) {
        if(id == null) return "ID不能为空";

        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_DELETE");
        message.put("id", id);

        mongoSender.send(message);
        if (redisTreeService.deleteWholeNodeByKey(id)) {
            return  "删除成功！";
        }

        return "删除失败！";
    }
}
