package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
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
        List<String> children = (List<String>) lGet(childrenKey, 0, -1);
        if(children == null || children.size() == 0) {

        }
        else {
            for(String child : children) {
                KnowledgeTreeNode childNode = findByKey(child);
            }
        }
        //父节点列表
        List<String> parents = (List<String>) lGet(parentsKey, 0, -1);
        //关联对象列表
        HashMap<String, String> objects = (HashMap<String, String>) hmget(objectsKey);
        KnowledgeTreeNode knowledgeTreeNode = new KnowledgeTreeNode(id, name, template, parents, children, objects);
        //设置时间属性
        knowledgeTreeNode.setCreateTime(createTime);
        knowledgeTreeNode.setUpdateTime(updateTime);
        return knowledgeTreeNode;
    }
}
