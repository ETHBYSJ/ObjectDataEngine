package com.sjtu.objectdataengine.service.tree;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
import com.sjtu.objectdataengine.utils.Result.*;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class APITreeService {

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
    public ResultInterface create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return Result.build(ResultCodeEnum.TREE_CREATE_EMPTY_ID);
        String name = jsonObject.getString("name");
        if (name == null) return Result.build(ResultCodeEnum.TREE_CREATE_EMPTY_NAME);
        String intro = jsonObject.getString("intro");
        if (intro == null) intro = "";
        String parent = jsonObject.getString("parent");
        if (parent  == null) return Result.build(ResultCodeEnum.TREE_CREATE_EMPTY_PARENT);

        List<String> children = new ArrayList<>();
        Date date = new Date();
        //组装message
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_CREATE");
        message.put("id", id);
        message.put("name", name);
        message.put("parent", parent);
        message.put("intro", intro);
        message.put("date", date);

        // 双写
        mongoSender.send(message);
        if(redisTreeService.createTreeNode(id, name, intro, parent, children, date))//redisTreeService.createTreeNode(id, name, template, parentsArray, children)) //改成上面参数的形式
           return Result.build(ResultCodeEnum.TREE_CREATE_SUCCESS);
        //如果传入id重复则会执行到此处并报错
        //this.delete(id);
        return Result.build(ResultCodeEnum.TREE_CREATE_FAIL);
    }

    /**
     * 删除整个子树
     * @param id 节点id
     * @return
     */
    public ResultInterface delSubtree(String id) {
        // ID不能为空
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.SUBTREE_DELETE_EMPTY_ID);
        TreeNode treeNode = redisTreeService.findNodeByKey(id);
        // 检查节点有效性
        if (treeNode == null) return Result.build(ResultCodeEnum.SUBTREE_DELETE_NODE_NOT_FOUND);
        String template = treeNode.getTemplate();
        Date date = new Date();
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "SUBTREE_DELETE");
        message.put("id", id);
        message.put("date", date);

        mongoSender.send(message);
        if(redisTreeService.deleteSubtree(id, date)) {
            return Result.build(ResultCodeEnum.SUBTREE_DELETE_SUCCESS);
        }
        return Result.build(ResultCodeEnum.SUBTREE_DELETE_FAIL);
    }
    public ResultInterface delete(String id) {
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.TREE_DELETE_EMPTY_ID);

        TreeNode treeNode = redisTreeService.findNodeByKey(id);
        if (treeNode == null) return Result.build(ResultCodeEnum.TREE_DELETE_NODE_NOT_FOUND);

        String template = treeNode.getTemplate();
        Date date = new Date();
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_DELETE");
        message.put("id", id);
        message.put("template", template);
        message.put("date", date);

        mongoSender.send(message);
        if (redisTreeService.deleteWholeNodeByKey(id, date)) { //删除node要把对应template删了
            return Result.build(ResultCodeEnum.TREE_DELETE_SUCCESS);
        }
        return Result.build(ResultCodeEnum.TREE_DELETE_FAIL);
    }

    /**
     * 修改name
     * 修改template 为空表示没有
     * 修改parents
     * @param request 请求体
     * @return 结果说明
     */
    public ResultInterface modify(String request) {
        Date date = new Date();
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "NODE_MODIFY");
        // id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return Result.build(ResultCodeEnum.TREE_MODIFY_EMPTY_ID);
        if(!redisTreeService.hasKey(id)) return Result.build(ResultCodeEnum.TREE_MODIFY_NODE_NOT_FOUND);     // 不存在
        modifyMessage.put("id", id);
        // name如果是null就不需要改
        String name = jsonObject.getString("name");
        if(name != null) {
            modifyMessage.put("name", name);
        }
        // intro如果是null就不需要改
        String intro = jsonObject.getString("intro");
        if(intro != null) {
            modifyMessage.put("intro", intro);
        }

        // 判断parents是否修改
        String parent = jsonObject.getString("parent");
        if(parent != null) {
            modifyMessage.put("parent", parent);
        }
        modifyMessage.put("date", date);
        mongoSender.send(modifyMessage);

        if (redisTreeService.updateNodeByKey(id, name, intro, parent, date)) {
            return Result.build(ResultCodeEnum.TREE_MODIFY_SUCCESS);
        }
        return Result.build(ResultCodeEnum.TREE_MODIFY_FAIL);
    }

    public ResultInterface getTreeByRoot(String id) {
        TreeNodeReturn treeNodeReturn = redisTreeService.findTreeByRoot(id);
        if(treeNodeReturn == null) {
            return ResultData.build(ResultCodeEnum.TREE_GET_FAIL, null);
        }
        return ResultData.build(ResultCodeEnum.TREE_GET_SUCCESS, treeNodeReturn);
    }

    public ResultInterface getNodeById(String id) {
        TreeNode treeNode = redisTreeService.findNodeByKey(id);
        if(treeNode == null) {
            return ResultData.build(ResultCodeEnum.NODE_GET_FAIL, null);
        }
        return ResultData.build(ResultCodeEnum.NODE_GET_SUCCESS, treeNode);
    }

    // public TreeNode getNodeByName(String name) {
    //     return redisTreeService.findNodeByName(name); //改成name
    // }
}
