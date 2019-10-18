package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoObjectDAO extends MongoBaseDAO<MongoObject> {

    /**
     * 查找全部
     * @return list
     */
    @Override
    public List<MongoObject> findAll() {
        return mongoTemplate.findAll(MongoObject.class);
    }

    /**
     * 根据对象主键（对象id）查询对象
     * @param key 主键key
     * @return 对象
     */
    @Override
    public MongoObject findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, MongoObject.class);
    }


    @Override
    public List<MongoObject> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    @Override
    public boolean update(MongoCondition mongoCondition) {
        return false;
    }

    @Override
    public List<MongoObject> fuzzySearch(String search) {
        return null;
    }
}
