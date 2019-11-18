package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.model.TreeNodeReturn;
import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.redis.RedisTemplateService;
import com.sjtu.objectdataengine.service.redis.RedisTreeService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.swing.tree.TreeNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Component
public class TreeService {

    @Resource
    private MongoSender mongoSender;

    @Resource
    private RedisTreeService redisTreeService;

    @Resource
    private RedisTemplateService redisTemplateService;


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

        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("template", template);
        message.put("parents", parentsArray);

        // 双写
        mongoSender.send(message);
        if(redisTreeService.createTreeNode(id, name, template, parentsArray, children))
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

    /**
     * 修改name
     * 修改template 为空表示没有
     * 修改parents
     * @param request 请求体
     * @return 结果说明
     */
    public String modify(String request) {
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "NODE_MODIFY");
        // id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        if(!redisTreeService.hasKey(id)) {
            //不存在
            return "ID不存在";
        }
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if(name != null) {
            modifyMessage.put("name", name);
        }
        // 判断template是否存在
        String template = jsonObject.getString("template");
        if(template != null && !template.equals("") && redisTemplateService.findTemplateById(template) == null) {
            return "模板不存在";
        }
        modifyMessage.put("template", template);
        // 判断parents是否修改
        String parents = jsonObject.getString("parents");

        List<String> parentsArray = new ArrayList<>();
        if(parents != null) {
            parentsArray.add(parents);
            modifyMessage.put("parents", parentsArray);
        } else {
            parentsArray = null;
        }

        mongoSender.send(modifyMessage);
        if (redisTreeService.updateNodeByKey(id, name, template, parentsArray)) {
            return "修改成功";
        }
        return "修改失败";
    }

    public TreeNodeReturn getTreeByRoot(String id) {
        return redisTreeService.findTreeByRoot(id);
    }

    public KnowledgeTreeNode getNodeById(String id) {
        return redisTreeService.findNodeByKey(id);
    }
}
