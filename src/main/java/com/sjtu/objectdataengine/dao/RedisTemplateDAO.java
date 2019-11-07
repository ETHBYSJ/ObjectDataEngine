package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.ObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RedisTemplateDAO extends RedisDAO {
    private RedisTemplate<String, Object> templateRedisTemplate;
    @Autowired
    public RedisTemplateDAO(RedisTemplate<String, Object> templateRedisTemplate) {
        super.setTemplate(templateRedisTemplate);
    }

    /**
     * 返回所有对象模板
     * @return 所有对象模板
     */
    public List<ObjectTemplate> findAll() {
        String indexKey = "index";
        Set<Object> indexSet = sGet(indexKey);
        List<ObjectTemplate> retList = new ArrayList<ObjectTemplate>();
        for(Object id : indexSet) {
            //依次根据id查询
            ObjectTemplate objectTemplate = findById(id.toString());
            if(objectTemplate != null) {
                retList.add(objectTemplate);
            }
        }
        return retList;
    }

    /**
     * 根据模板id查找模板
     * @param id 模板id
     * @return 模板
     */
    public ObjectTemplate findById(String id) {
        String attrsKey = id + '#' + "attrs";
        String baseKey = id + '#' + "base";
        //基本信息
        Object name = hget(baseKey, "name");
        if(name == null) return null;
        Object type = hget(baseKey, "type");
        if(type == null) return null;
        Object nodeId = hget(baseKey, "nodeId");
        if(nodeId == null) return null;
        Object createTime = hget(baseKey, "createTime");
        Object updateTime = hget(baseKey, "updateTime");

        Set<Object> attrs = sGet(attrsKey);
        //类型转换
        Set<String> attrSet = new HashSet<String>();
        for(Object attr : attrs) {
            attrSet.add(attr.toString());
        }
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name.toString(), attrSet, nodeId.toString(), type.toString());
        objectTemplate.setCreateTime((Date) createTime);
        objectTemplate.setUpdateTime((Date) updateTime);
        return objectTemplate;
    }

    /**
     * 根据模板id删除模板
     * @param id 模板id
     * @return true代表删除成功，false代表删除失败
     */
    public boolean deleteById(String id) {
        String indexKey = "index";
        String attrsKey = id + '#' + "attrs";
        String baseKey = id + '#' + "base";
        if(sHasKey(indexKey, id)) {
            //如果在索引表中存在此id，则删除
            setRemove(indexKey, id);
            //删除模板数据
            hdel(baseKey, "id", "name", "type", "nodeId", "createTime", "updateTime");
            long size = sGetSetSize(attrsKey);
            setPop(attrsKey, size);
            return true;
        }
        else {
            //索引表中不存在此id，删除失败
            return false;
        }
    }
}