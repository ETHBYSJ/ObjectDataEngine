package com.sjtu.objectdataengine.dao.tree;

import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class RedisTreeDAO extends RedisDAO {
    private RedisTemplate<String, Object> treeRedisTemplate;
    @Autowired
    public RedisTreeDAO(RedisTemplate<String, Object> treeRedisTemplate) {
        super.setTemplate(treeRedisTemplate);
    }
    public boolean deleteByKey(String key) {
        try {
            String baseKey = key + '#' + "base";
            String childrenKey = key + '#' + "children";
            String parentsKey = key + '#' + "parents";
            String objectsKey = key + '#' + "objects";
            String indexKey = "index";
            //如果已经删除，直接返回即可
            if(!sHasKey(indexKey, key)) {
                return true;
            }
            //删除基本信息
            del(baseKey);
            //删除子节点列表
            del(childrenKey);
            //删除父节点列表
            del(parentsKey);
            //删除关联对象列表
            del(objectsKey);
            //从索引表中删除
            setRemove(indexKey, key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据id返回指定树节点
     * @param key 树节点id
     * @return 树节点(无嵌套)
     */
    public TreeNode findByKey(String key) {
        String indexKey = "index";
        //如果没有找到，直接返回
        if(!sHasKey(indexKey, key)) {
            return null;
        }
        String baseKey = key + '#' + "base";
        String childrenKey = key + '#' + "children";
        String objectsKey = key + '#' + "objects";
        String id = hget(baseKey, "id").toString();
        String name = hget(baseKey, "name") == null ? "" : hget(baseKey, "name").toString();
        String intro = hget(baseKey, "intro") == null ? "" : hget(baseKey, "intro").toString();
        String template = hget(baseKey, "template") == null ? "" : hget(baseKey, "template").toString();
        Date createTime = (Date) hget(baseKey, "createTime");
        Date updateTime = (Date) hget(baseKey, "updateTime");
        //孩子节点列表
        List<String> children = (List<String>) lGet(childrenKey, 0, -1);
        //父节点列表
        String parent = hget(baseKey, "parent").toString();
        //关联对象列表
        HashMap<String, String> objects = (HashMap<String, String>) hmget(objectsKey);
        TreeNode treeNode = new TreeNode(id, name, intro, template, parent, children);
        treeNode.setCreateTime(createTime);
        treeNode.setUpdateTime(updateTime);
        return treeNode;
    }
    /**
     * 根据id返回指定树节点
     * @param key 树节点id
     * @return 树节点
     */
    public TreeNodeReturn findTreeByRoot(String key) {
        String indexKey = "index";
        //如果没有找到，直接返回
        if(!sHasKey(indexKey, key)) {
            return null;
        }
        String baseKey = key + '#' + "base";
        String childrenKey = key + '#' + "children";
        String parentsKey = key + '#' + "parents";
        String objectsKey = key + '#' + "objects";
        String id = hget(baseKey, "id").toString();
        String name = hget(baseKey, "name") == null ? "" : hget(baseKey, "name").toString();
        String intro = hget(baseKey, "intro") == null ? "" : hget(baseKey, "intro").toString();
        String template = hget(baseKey, "template") == null ? "" : hget(baseKey, "template").toString();
        Date createTime = (Date) hget(baseKey, "createTime");
        Date updateTime = (Date) hget(baseKey, "updateTime");
        //孩子节点列表
        List<String> childrenList = (List<String>) lGet(childrenKey, 0, -1);
        List<TreeNodeReturn> children = new ArrayList<TreeNodeReturn>();
        if(childrenList == null || childrenList.size() == 0) {

        }
        else {
            for(String child : childrenList) {
                TreeNodeReturn childNode = findTreeByRoot(child);
                children.add(childNode);
            }
        }
        //父节点列表
        List<String> parents = (List<String>) lGet(parentsKey, 0, -1);
        //关联对象列表
        HashMap<String, String> objects = (HashMap<String, String>) hmget(objectsKey);
        TreeNodeReturn treeNodeReturn = new TreeNodeReturn(id, name, intro, template, parents, children, objects);
        //设置时间属性
        treeNodeReturn.setCreateTime(createTime);
        treeNodeReturn.setUpdateTime(updateTime);
        return treeNodeReturn;
    }
}
