package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.AttrsHeader;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoHeaderDAO extends MongoBaseDAO<AttrsHeader> {

    @Override
    public List<AttrsHeader> findAll() {
        return mongoTemplate.findAll(AttrsHeader.class);
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
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, AttrsHeader.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<AttrsHeader> fuzzySearch(String search) {
        return null;
    }
}
