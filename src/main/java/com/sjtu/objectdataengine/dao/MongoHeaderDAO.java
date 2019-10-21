package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.AttrsHeader;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoHeaderDAO extends MongoBaseDAO<AttrsHeader> {

    @Override
    public List<AttrsHeader> findAll() {
        return null;
    }

    @Override
    public AttrsHeader findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, AttrsHeader.class);
    }

    @Override
    public List<AttrsHeader> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    @Override
    public boolean update(MongoCondition mongoCondition) {
        return false;
    }

    @Override
    public List<AttrsHeader> fuzzySearch(String search) {
        return null;
    }
}
