package com.sjtu.objectdataengine.service.mongodb;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.redis.RedisTemplateService;
import com.sjtu.objectdataengine.service.redis.RedisTreeService;
import com.sjtu.objectdataengine.utils.TypeConversion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;

@Component
public class TemplateService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisTemplateService redisTemplateService;

    @Resource
    RedisTreeService redisTreeService;

    public String create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        String name = jsonObject.getString("name");
        if (name == null) name = "";
        String nodeId = jsonObject.getString("nodeId");
        if (nodeId == null) nodeId = "";
        String type = jsonObject.getString("type");
        if (type == null) return "类型不能为空";
        JSONObject attrsJson = jsonObject.getJSONObject("attrs");
        if (attrsJson == null) return "属性不能为空";
        HashMap<String, String> attrs = TypeConversion.JsonToMap(attrsJson);
        //redisTemplateService.createTemplate(id, name, type, nodeId, attrs);

        HashMap<String, Object> createMessage = new HashMap<>();
        createMessage.put("op", "TEMP_CREATE");
        createMessage.put("id", id);
        createMessage.put("name", name);
        createMessage.put("nodeId", nodeId);
        createMessage.put("type", type);
        createMessage.put("attrs", attrs);

        mongoSender.send(createMessage);

        if(redisTemplateService.createTemplate(id, name, type, nodeId, attrs)) {
            return "创建成功";
        }
        this.delete(id);
        return "创建失败!";
    }

    public String delete(String id) {
        if(id == null) return "ID不能为空";

        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(id);
        if (objectTemplate == null) return "没有该模板";

        String nodeId = objectTemplate.getNodeId();

        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "TEMP_DELETE");
        message.put("id", id);
        message.put("nodeId", nodeId);

        mongoSender.send(message);
        if (redisTemplateService.deleteTemplateById(id)) {
            return  "删除成功！";
        }
        return "删除失败！";
    }

    public String modifyBaseInfo(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "TEMP_MODIFY");
        // id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        if(!redisTemplateService.hasKey(id)) return "ID不存在";     // 不存在
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if(name != null) {
            modifyMessage.put("name", name);
        }
        // 判断nodeId是否存在
        String nodeId = jsonObject.getString("nodeId");
        if(nodeId!= null && !nodeId.equals("") && redisTreeService.findNodeByKey(nodeId) == null) {
            return "结点不存在";
        }

        //String nodeId =
        return "";
    }

}
