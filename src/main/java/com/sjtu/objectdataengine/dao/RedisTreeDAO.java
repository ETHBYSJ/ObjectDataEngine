package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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
            hdel(baseKey, "id", "name", "template", "createTime", "updateTime");
            //删除子节点列表
            lTrim(childrenKey, 1, 0);
            //删除父节点列表
            lTrim(parentsKey, 1, 0);
            //删除关联对象列表
            lTrim(objectsKey, 1, 0);
            //从索引表中删除
            long l = setRemove(indexKey, key);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 根据id返回指定树节点
     * @param key 树节点id
     * @return 树节点
     */
    public KnowledgeTreeNode findByKey(String key) {
        String baseKey = key + '#' + "base";
        String childrenKey = key + '#' + "children";
        String parentsKey = key + '#' + "parents";
        String objectsKey = key + '#' + "objects";
        String id = hget(baseKey, "id").toString();
        String name = hget(baseKey, "name") == null ? "" : hget(baseKey, "name").toString();
        String template = hget(baseKey, "template") == null ? "" : hget(baseKey, "template").toString();
        Date createTime = (Date) hget(baseKey, "createTime");
        Date updateTime = (Date) hget(baseKey, "updateTime");
        //孩子节点列表
        /*
        List<String> children = new ArrayList<String>();
        List<Object> childrenList = lGet(childrenKey, 0, -1);
        //类型转换
        if(childrenList != null) {
            for(Object child : childrenList) {
                children.add(child.toString());
            }
        }
        */
        List<KnowledgeTreeNode> children = new ArrayList<KnowledgeTreeNode>();
        List<Object> childrenList = lGet(childrenKey, 0, -1);
        if(childrenList == null || childrenList.size() == 0) {

        }
        else {
            for(Object child : childrenList) {
                KnowledgeTreeNode childNode = findByKey(child.toString());
            }
        }
        //父节点列表
        List<String> parents = new ArrayList<String>();
        List<Object> parentsList = lGet(parentsKey, 0, -1);
        //类型转换
        if(parentsList != null) {
            for(Object parent : parentsList) {
                parents.add(parent.toString());
            }
        }
        //关联对象列表
        List<String> objects = new ArrayList<String>();
        List<Object> objectsList = lGet(objectsKey, 0, -1);
        //类型转换
        if(objectsList != null) {
            for(Object obj : objectsList) {
                objects.add(obj.toString());
            }
        }
        //KnowledgeTreeNode knowledgeTreeNode = new KnowledgeTreeNode(id, name, template, parents, children, objects);
        //设置时间属性
        //knowledgeTreeNode.setCreateTime(createTime);
        //knowledgeTreeNode.setUpdateTime(updateTime);
        //return knowledgeTreeNode;
        return null;
    }
}
