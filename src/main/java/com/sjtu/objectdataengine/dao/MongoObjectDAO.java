package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoAttrs;
import com.sjtu.objectdataengine.model.MongoBase;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

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
        return false;
    }

    @Override
    public List<MongoAttrs> fuzzySearch(String search) {
        return null;
    }





}
