package com.sjtu.objectdataengine.dao;

import com.mongodb.Mongo;
import com.sjtu.objectdataengine.model.*;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoObjectDAO extends MongoBaseDAO<MongoAttrs> {

    /**
     * 查找全部属性
     * @return list
     */
    @Override
    public List<MongoAttrs> findAll() {
        return mongoTemplate.findAll(MongoAttrs.class);
    }

    /**
     * 根据主键id查询某条属性
     * @param key 主键key
     * @return 对象
     */
    @Override
    public MongoAttrs findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, MongoAttrs.class);
    }

    @Override
    public List<MongoAttrs> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    @Override
    public boolean update(MongoCondition mongoCondition) {
        /*Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, MongoAttrs.class);
            return true;
        } catch (Exception e) {
            return false;
        }*/
        return true;
    }

    public boolean addValue(String key, MongoAttr mongoAttr) {
        try {
            MongoAttrs mongoAttrs = findByKey(key);
            int size = mongoAttrs.getSize();
            Query query = new Query();
            Update update = new Update();
            Criteria criteria = Criteria.where("_id").is(key);
            query.addCriteria(criteria);
            update.addToSet("attrs", mongoAttr);
            update.set("size", size+1);
            mongoTemplate.updateMulti(query, update, MongoAttrs.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<MongoAttrs> fuzzySearch(String search) {
        return null;
    }





}
