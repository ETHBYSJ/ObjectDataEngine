package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.model.RootMessage;
import com.sjtu.objectdataengine.model.TreeNodeReturn;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RedisRootDAO extends RedisDAO {
    private RedisTemplate<String, Object> treeRedisTemplate;
    @Autowired
    public RedisRootDAO(RedisTemplate<String, Object> treeRedisTemplate) {
        super.setTemplate(treeRedisTemplate);
    }

    /**
     * 创建新的根节点
     * @param rootMessage 根节点
     * @return true代表创建成功，false代表创建失败
     */
    public boolean create(RootMessage rootMessage) {
        try {
            HashMap<String, String> roots = rootMessage.getRoots();
            Date createTime = rootMessage.getCreateTime();
            Date updateTime = rootMessage.getUpdateTime();
            hset("root#base", "createTime", createTime);
            hset("root#base", "updateTime", updateTime);
            if(roots.size() != 0) {
                hmset("root#roots", roots);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean hasRootMessage() {
        return hasKey("root#base");
    }
    /**
     * 返回根节点
     * @return 根节点
     */
    public RootMessage findRoot() {
        Date createTime = (Date) hget("root#base", "createTime");
        Date updateTime = (Date) hget("root#base", "updateTime");
        Map<String, String> rootMap = (Map<String, String>) hmget("root#roots");
        HashMap<String, String> roots = new HashMap<String, String>();
        for(Map.Entry<String, String> entry : rootMap.entrySet()) {
            String id = entry.getKey();
            String name = entry.getValue();
            roots.put(id, name);
        }
        RootMessage rootMessage = new RootMessage("root", roots);
        return rootMessage;
    }

    /**
     * 添加新的根节点
     * @param id 节点id
     * @param name 节点名
     */
    public void addNewRoot(String id, String name) {
        hset("root#roots", id, name);
        hset("root#base", "updateTime", new Date());
    }

    /**
     * 查询指定根节点是否存在
     * @param id 节点id
     * @return true代表根节点已经存在，false代表根节点不存在
     */
    public boolean hasRoot(String id) {
        return hHasKey("root#roots", id);
    }
    public boolean deleteRoot(String id) {
        hset("root#base", "updateTime", new Date());
        return hdel("root#roots", id) > 0;
    }
}

