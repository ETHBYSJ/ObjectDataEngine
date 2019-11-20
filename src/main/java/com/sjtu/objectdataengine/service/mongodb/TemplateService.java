package com.sjtu.objectdataengine.service.mongodb;

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

        // 判断nodeId是否存在
        String nodeId = jsonObject.getString("nodeId");
        String oldNodeId = objectTemplate.getNodeId();
        // 改之后的nodeId对应的node
        TreeNode treeNode;
        String newTemplate; //要修改的新的node上面的template（原有的
        if (nodeId!= null && !nodeId.equals("") ) {
            treeNode = redisTreeService.findNodeByKey(nodeId);
            if (treeNode == null) return "结点不存在";
            else {
                newTemplate = treeNode.getTemplate();
                modifyMessage.put("nodeId", nodeId);
                modifyMessage.put("oldNodeId", oldNodeId);
                modifyMessage.put("newTemplate", newTemplate);
            }
        } else if (nodeId != null) { //nodeId有可能是""
            modifyMessage.put("nodeId", nodeId);
            modifyMessage.put("oldNodeId", oldNodeId);
            newTemplate = null;
        }
        // 判断type
        String type = jsonObject.getString("type");
        if (type != null) {
            modifyMessage.put("name", name);
        }

        System.out.println(modifyMessage);
        mongoSender.send(modifyMessage);
        if (redisTemplateService.updateBaseInfo(id, name, oldNodeId, nodeId, type)) {
            return "修改成功";
        }
        return "修改失败";
    }

}
