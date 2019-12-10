package com.sjtu.objectdataengine.service.tree;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import com.sjtu.objectdataengine.rabbitMQ.inside.sender.MongoSender;
import com.sjtu.objectdataengine.service.template.RedisTemplateService;
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
    public String create(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        String name = jsonObject.getString("name");
        if (name == null) return  "name不能为空";
        String intro = jsonObject.getString("intro");
        if (intro == null) intro = "";
        String parent = jsonObject.getString("parent");
        if (parent  == null) return "父节点不能为空";

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
           return "创建成功！";
        //如果传入id重复则会执行到此处并报错
        //this.delete(id);
        return "创建失败!";
    }

    public String delete(String id) {
        if(id == null || id.equals("")) return "ID不能为空";

        TreeNode treeNode = redisTreeService.findNodeByKey(id);
        if (treeNode == null) return "没有该节点";

        String template = treeNode.getTemplate();
        Date date = new Date();
        HashMap<String, Object> message = new HashMap<>();
        message.put("op", "NODE_DELETE");
        message.put("id", id);
        message.put("template", template);
        message.put("date", date);

        mongoSender.send(message);
        if (redisTreeService.deleteWholeNodeByKey(id, date)) { //删除node要把对应template删了
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
        Date date = new Date();
        // 解析
        JSONObject jsonObject = JSON.parseObject(request);
        HashMap<String, Object> modifyMessage = new HashMap<>();
        modifyMessage.put("op", "NODE_MODIFY");
        // id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return "ID不能为空";
        if(!redisTreeService.hasKey(id)) return "ID不存在";     // 不存在
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
            return "修改成功";
        }
        return "修改失败";
    }

    public TreeNodeReturn getTreeByRoot(String id) {
        return redisTreeService.findTreeByRoot(id);
    }

    public TreeNode getNodeById(String id) {
        return redisTreeService.findNodeByKey(id);
    }

    // public TreeNode getNodeByName(String name) {
    //     return redisTreeService.findNodeByName(name); //改成name
    // }
}
