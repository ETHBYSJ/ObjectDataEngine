package com.sjtu.objectdataengine.dao.template;

import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
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
        Set<String> indexSet = (Set<String>) sGet(indexKey);
        List<ObjectTemplate> retList = new ArrayList<ObjectTemplate>();
        for(String id : indexSet) {
            //依次根据id查询
            ObjectTemplate objectTemplate = findById(id);
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
        String objectsKey = id + '#' + "objects";
        //基本信息
        Object name = hget(baseKey, "name");
        if(name == null) return null;
        Object type = hget(baseKey, "type");
        if(type == null) return null;
        Object nodeId = hget(baseKey, "nodeId");
        if(nodeId == null) return null;
        String intro = hget(baseKey, "intro") == null ? "" : hget(baseKey, "intro").toString();
        //关联对象
        //HashMap<String, String> objects = (HashMap<String, String>) hmget(objectsKey);
        List<String> objects = (List<String>) lGet(objectsKey, 0, -1);
        if(objects == null) {
            objects = new ArrayList<String>();
        }
        Object createTime = hget(baseKey, "createTime");
        Object updateTime = hget(baseKey, "updateTime");
        //属性
        HashMap<String, String> attrs = (HashMap<String, String>) hmget(attrsKey);
        if(attrs == null) {
            attrs = new HashMap<String, String>();
        }
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name.toString(), intro, nodeId.toString(), type.toString(), attrs, objects);
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
        String objectsKey = id + '#' + "objects";
        if(sHasKey(indexKey, id)) {
            //如果在索引表中存在此id，则删除
            setRemove(indexKey, id);
            //删除模板数据
            del(baseKey);
            del(attrsKey);
            del(objectsKey);
        }
        return true;
        /*
        else {
            //索引表中不存在此id，删除失败
            return false;
        }
        */
    }
    public boolean opObject(String id, String objId, String op) {
        if(op.equals("add")) {
            if(!lHasValue(id + "#objects", objId)) {
                return lSet(id + "#objects", objId);
            }
            else return false;
        }
        else if(op.equals("del")) {
            return lRemove(id + "#objects", 1, objId) > 0;
        }
        return false;
    }
    public boolean opEvent(String id, String objId, String op) {
        if(op.equals("add")) {
            if(!lHasValue(id + "#events", objId)) {
                return lSet(id + "#events", objId);
            }
            else return false;
        }
        else if(op.equals("del")) {
            return lRemove(id + "#events", 1, objId) > 0;
        }
        return false;
    }
}