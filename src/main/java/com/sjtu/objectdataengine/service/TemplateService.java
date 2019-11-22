package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.TreeNode;
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
        if (name == null) return "name不能为空";
        String intro = jsonObject.getString("intro");
        if (intro == null) intro = "";
        String nodeId = jsonObject.getString("nodeId");
        if (nodeId == null || nodeId.equals("")) return "nodeId不能为空";
        // 检查nodeId
        TreeNode treeNode = redisTreeService.findNodeByKey(nodeId);
        if(treeNode == null) {
            return "指定的nodeId不存在";
        } else if(!treeNode.getTemplate().equals("")) {
            return "指定的node上已存在模板，若想创建新模板请先删除旧模板";
        }
        String type = jsonObject.getString("type");
        if (type == null || type.equals("")) return "类型不能为空";
        JSONObject attrsJson = jsonObject.getJSONObject("attrs");
        if (attrsJson == null) return "属性不能为空";
        HashMap<String, String> attrs = TypeConversion.JsonToMap(attrsJson);

        HashMap<String, Object> createMessage = new HashMap<>();
        createMessage.put("op", "TEMP_CREATE");
        createMessage.put("id", id);
        createMessage.put("name", name);
        createMessage.put("intro", intro);
        createMessage.put("nodeId", nodeId);
        createMessage.put("type", type);
        createMessage.put("attrs", attrs);

        mongoSender.send(createMessage);

        if(redisTemplateService.createTemplate(id, name, intro, type, nodeId, attrs)) {
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
        modifyMessage.put("op", "TEMP_MODIFY_BASE");
        // id必须要有
        String id = jsonObject.getString("id");
        if (id == null) return "ID不能为空";
        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(id);
        if (objectTemplate == null) return "ID不存在";     // 不存在
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if (name != null) {
            modifyMessage.put("name", name);
        }
        // intro如果是null就不需要改
        String intro = jsonObject.getString("name");
        if (intro != null) {
            modifyMessage.put("intro", intro);
        }
        // System.out.println(modifyMessage);
        mongoSender.send(modifyMessage);
        if (redisTemplateService.updateBaseInfo(id, name, intro)) {
            return "修改成功";
        }
        return "修改失败";
    }

    public String addAttr(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        // id
        String id = jsonObject.getString("id");
        if (id == null || id.equals("")) return "ID不能为空";
        if (redisTemplateService.hasKey(id)) return "ID不存在";
        // name
        String name = jsonObject.getString("name");
        if (name == null || name.equals("")) return "name不能为空";
        // intro
        String intro = jsonObject.getString("intro");
        if (intro == null) return "intro必须指定";
        HashMap<String, Object> addAttrMessage = new HashMap<>();
        addAttrMessage.put("op", "TEMP_ADD_ATTR");

        mongoSender.send(addAttrMessage);
        return "添加成功";
    }

    public String delAttr() {
        return "删除成功";
    }
}
