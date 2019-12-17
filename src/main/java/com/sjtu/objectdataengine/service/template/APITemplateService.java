package com.sjtu.objectdataengine.service.template;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.tree.RedisTreeService;
import com.sjtu.objectdataengine.utils.Result.*;
import com.sjtu.objectdataengine.utils.TypeConversion;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;

@Component
public class APITemplateService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisTemplateService redisTemplateService;

    @Resource
    MongoTemplateService mongoTemplateService;

    @Resource
    RedisTreeService redisTreeService;

    @Resource
    SubscribeService subscribeService;

    public ResultInterface create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return Result.build(ResultCodeEnum.TEMPLATE_CREATE_EMPTY_ID);
        String name = jsonObject.getString("name");
        if (name == null) return Result.build(ResultCodeEnum.TEMPLATE_CREATE_EMPTY_NAME);
        String intro = jsonObject.getString("intro");
        if (intro == null) intro = "";
        String nodeId = jsonObject.getString("nodeId");
        if (nodeId == null || nodeId.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_CREATE_EMPTY_NODEID);
        // 检查nodeId
        TreeNode treeNode = redisTreeService.findNodeByKey(nodeId);
        if(treeNode == null) {
            return Result.build(ResultCodeEnum.TEMPLATE_CREATE_NODE_NOT_FOUND);
        } else if(!treeNode.getTemplate().equals("")) {
            return Result.build(ResultCodeEnum.TEMPLATE_CREATE_ALREADY_EXISTS);
        }
        // 检查type
        String type = jsonObject.getString("type");
        if (type == null || type.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_CREATE_TYPE_EMPTY);
        // 检查attrs
        JSONObject attrsJson = jsonObject.getJSONObject("attrs");
        if (attrsJson == null) return Result.build(ResultCodeEnum.TEMPLATE_CREATE_ATTRS_EMPTY);
        HashMap<String, String> attrs = TypeConversion.JsonToMap(attrsJson);
        Date date = new Date();
        HashMap<String, Object> createMessage = new HashMap<>();
        createMessage.put("op", "TEMP_CREATE");
        createMessage.put("id", id);
        createMessage.put("name", name);
        createMessage.put("intro", intro);
        createMessage.put("nodeId", nodeId);
        createMessage.put("type", type);
        createMessage.put("attrs", attrs);
        createMessage.put("date", date);

        mongoSender.send(createMessage);

        if(redisTemplateService.createTemplate(id, name, intro, type, nodeId, attrs, date)) {
            // 创建订阅表
            subscribeService.create(id, "template");
            return Result.build(ResultCodeEnum.TEMPLATE_CREATE_SUCCESS);
        }
        //this.delete(id);
        return Result.build(ResultCodeEnum.TEMPLATE_CREATE_FAIL);
    }

    public ResultInterface delete(String id) {
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_DELETE_EMPTY_ID);

        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(id);
        if (objectTemplate == null) return Result.build(ResultCodeEnum.TEMPLATE_DELETE_NOT_FOUND);

        String nodeId = objectTemplate.getNodeId();

        HashMap<String, Object> message = new HashMap<>();
        Date date = new Date();
        message.put("op", "TEMP_DELETE");
        message.put("id", id);
        message.put("nodeId", nodeId);
        message.put("date", date);

        mongoSender.send(message);
        if (redisTemplateService.deleteTemplateById(id, date)) {
            return Result.build(ResultCodeEnum.TEMPLATE_DELETE_SUCCESS);
        }
        return Result.build(ResultCodeEnum.TEMPLATE_DELETE_FAIL);
    }

    public ResultInterface modifyBaseInfo(String request) {
        Date date = new Date();
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "TEMP_MODIFY_BASE");
        modifyMessage.put("date", date);
        // id必须要有
        String id = jsonObject.getString("id");
        if (id == null || id.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_MODIFY_EMPTY_ID);
        ObjectTemplate objectTemplate = redisTemplateService.findTemplateById(id);
        if (objectTemplate == null) return Result.build(ResultCodeEnum.TEMPLATE_MODIFY_NOT_FOUND);     // 不存在
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if (name != null) {
            modifyMessage.put("name", name);
        }
        // intro如果是null就不需要改
        String intro = jsonObject.getString("intro");
        if (intro != null) {
            modifyMessage.put("intro", intro);
        }
        // System.out.println(modifyMessage);
        mongoSender.send(modifyMessage);
        if (redisTemplateService.updateBaseInfo(id, name, intro, date)) {
            return Result.build(ResultCodeEnum.TEMPLATE_MODIFY_SUCCESS);
        }
        return Result.build(ResultCodeEnum.TEMPLATE_MODIFY_FAIL);
    }

    public ResultInterface addAttr(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        // id
        String id = jsonObject.getString("id");
        if (id == null || id.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_EMPTY_ID);
        if (redisTemplateService.hasKey(id)) return Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_NOT_FOUND);
        // name
        String name = jsonObject.getString("name");
        if (name == null || name.equals("")) Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_EMPTY_NAME);
        // nickname
        String nickname = jsonObject.getString("intro");
        if (nickname == null || nickname.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_EMPTY_INTRO);

        HashMap<String, Object> opAttrMessage = new HashMap<>();
        Date date = new Date();
        opAttrMessage.put("op", "TEMP_ADD_ATTR");
        opAttrMessage.put("id", id);
        opAttrMessage.put("name", name);
        opAttrMessage.put("nickname", nickname);
        opAttrMessage.put("date", date);

        mongoSender.send(opAttrMessage);
        if (redisTemplateService.addAttrs(id, name, nickname, date)) {
            return Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_SUCCESS);
        } else {
            opAttrMessage.put("op", "TEMP_DEL_ATTR");
            opAttrMessage.remove("nickname");
            mongoSender.send(opAttrMessage);
            return Result.build(ResultCodeEnum.TEMPLATE_ADD_ATTR_FAIL);
        }
    }

    public ResultInterface delAttr(String id, String name) {
        Date date = new Date();
        if (id == null || id.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_DEL_ATTR_EMPTY_ID);
        if (!redisTemplateService.hasKey(id)) return Result.build(ResultCodeEnum.TEMPLATE_DEL_ATTR_NOT_FOUND);
        // name
        if (name == null || name.equals("")) return Result.build(ResultCodeEnum.TEMPLATE_DEL_ATTR_EMPTY_NAME);
        //if (!redisTemplateService.keyHasName(id, name)) return "name不存在";

        HashMap<String, Object> delAttrMessage = new HashMap<>();

        delAttrMessage.put("op", "TEMP_DEL_ATTR");
        delAttrMessage.put("id", id);
        delAttrMessage.put("name", name);
        delAttrMessage.put("date", date);

        mongoSender.send(delAttrMessage);
        if (redisTemplateService.delAttrs(id, name, date)) {
            return Result.build(ResultCodeEnum.TEMPLATE_DEL_ATTR_SUCCESS);
        }

        return Result.build(ResultCodeEnum.TEMPLATE_DEL_ATTR_FAIL);
    }

    public ResultInterface get(String id) {
        if (id == null || id.equals("")) return null;
        ObjectTemplate objectTemplate = mongoTemplateService.findTemplateById(id);
        if(objectTemplate == null) {
            return ResultData.build(ResultCodeEnum.TEMPLATE_GET_FAIL, null);
        }
        return ResultData.build(ResultCodeEnum.TEMPLATE_GET_SUCCESS, objectTemplate);
    }
}
