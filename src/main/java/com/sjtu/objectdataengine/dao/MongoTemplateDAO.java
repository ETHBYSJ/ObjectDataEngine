package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
public class MongoTemplateDAO extends MongoBaseDAO<ObjectTemplate>{

    private MongoTemplate mongoTemplate;

    /**
     * 创建对象
     * @param objectTemplate 存储对象
     * @return 布尔，1表示成功
     */
    @Override
    public boolean create(ObjectTemplate objectTemplate) {
        objectTemplate.setCreateTime(new Date());
        objectTemplate.setUpdateTime(new Date());
        try {
            mongoTemplate.insert(objectTemplate);
            return true;
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询全部
     * @return String类型List，返回集合所有数据
     */
    @Override
    public List<ObjectTemplate> findAll() {
        return mongoTemplate.findAll(ObjectTemplate.class);
    }

    /**
     * 根据主键key查询
     * @param key 主键key
     * @return ObjectTemplate类型，返回某条数据
     */
    @Override
    public ObjectTemplate findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, ObjectTemplate.class);
    }

    /**
     * 根据其他关键字查询
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<ObjectTemplate> findByArgs(MongoCondition mongoCondition) {
        Query query = new Query();
        Map<String, String> queryMap = mongoCondition.getQueryMap();
        for(Map.Entry<String, String> entry : queryMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            query.addCriteria(Criteria.where(mapKey).is(mapValue));
        }
        return mongoTemplate.find(query, ObjectTemplate.class);
    }

    /**
     * 更新对象
     * @param mongoCondition 更新条件
     */
    @Override
    public void update(MongoCondition mongoCondition) {
        Query query = new Query();
        Update update = new Update();
        Map<String, String> queryMap = mongoCondition.getQueryMap();
        Map<String, String> updateMap = mongoCondition.getUpdateMap();
        for(Map.Entry<String, String> entry : queryMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            query.addCriteria(Criteria.where(mapKey).is(mapValue));
        }
        for(Map.Entry<String, String> entry : updateMap.entrySet()) {
            String mapKey = entry.getKey();
            String mapValue = entry.getValue();
            update.set(mapKey, mapValue);
        }
        mongoTemplate.updateMulti(query, update, ObjectTemplate.class);
    }

    /**
     * 根据键值删除对象
     * @param key 键
     */
    @Override
    public void deleteByKey(String key)  {
        ObjectTemplate objectTemplate = findByKey(key);
        mongoTemplate.remove(objectTemplate);
    }

    /**
     * 根据查询条件删除对象
     * @param mongoCondition 删除条件
     */
    @Override
    public void deleteByArgs(MongoCondition mongoCondition) {
        List<ObjectTemplate> objectTemplates = findByArgs(mongoCondition);
        for (ObjectTemplate objectTemplate : objectTemplates) {
            mongoTemplate.remove(objectTemplate);
        }
    }

    @Override
    public List<ObjectTemplate> fuzzySearch(String search) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + search + ".*$" , Pattern.CASE_INSENSITIVE);
        Criteria criteria = Criteria.where("name").regex(pattern);
        query.addCriteria(criteria);
        return mongoTemplate.findAllAndRemove(query, ObjectTemplate.class);
    }
}
