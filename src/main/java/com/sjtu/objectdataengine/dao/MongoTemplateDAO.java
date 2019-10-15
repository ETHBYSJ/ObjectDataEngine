package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Pattern;

@Component
public class MongoTemplateDAO extends MongoBaseDAO<ObjectTemplate>{
    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 创建对象
     * @param objectTemplate 存储对象
     * @return 布尔，1表示成功
     */
    @Override
    public boolean create(ObjectTemplate objectTemplate) {
        Date date = new Date();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Shanghai"));
        System.out.println(date);
        objectTemplate.setCreateTime(date);
        objectTemplate.setUpdateTime(date);
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
        //System.out.println(query);
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
        //System.out.println(query);
        return mongoTemplate.find(query, ObjectTemplate.class);
    }

    /**
     * 更新对象
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
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
        try {
            mongoTemplate.updateMulti(query, update, ObjectTemplate.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 根据键值删除对象
     * @param key 键
     */
    @Override
    public boolean deleteByKey(String key)  {
        ObjectTemplate objectTemplate = findByKey(key);
        try {
            mongoTemplate.remove(objectTemplate);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据查询条件删除对象
     * @param mongoCondition 删除条件
     */
    @Override
    public boolean deleteByArgs(MongoCondition mongoCondition) {
        List<ObjectTemplate> objectTemplates = findByArgs(mongoCondition);
        try {
            for (ObjectTemplate objectTemplate : objectTemplates) {
                mongoTemplate.remove(objectTemplate);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 模糊查询
     * @param search 查询条件
     * @return 查询结果
     */
    @Override
    public List<ObjectTemplate> fuzzySearch(String search) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + search + ".*$" , Pattern.CASE_INSENSITIVE);
        Criteria criteria = Criteria.where("name").regex(pattern);
        query.addCriteria(criteria);
        return mongoTemplate.findAllAndRemove(query, ObjectTemplate.class);
    }
}
