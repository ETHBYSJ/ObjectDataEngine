package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MongoTreeDAO extends MongoBaseDAO<KnowledgeTreeNode> {

    @Override
    public List<KnowledgeTreeNode> findAll() {
        return mongoTemplate.findAll(KnowledgeTreeNode.class);
    }

    @Override
    public KnowledgeTreeNode findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, KnowledgeTreeNode.class);
    }

    @Override
    public List<KnowledgeTreeNode> findByArgs(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        return mongoTemplate.find(query, KnowledgeTreeNode.class);
    }

    @Override
    public boolean update(MongoCondition mongoCondition) {
       try {
            Query query = mongoCondition.getQuery();
            Update update = mongoCondition.getUpdate();
            update.set("updateTime", new Date());
            System.out.println(query);
            System.out.println(update);
            mongoTemplate.updateMulti(query, update, KnowledgeTreeNode.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<KnowledgeTreeNode> fuzzySearch(String search) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + search + ".*$" , Pattern.CASE_INSENSITIVE);
        Criteria criteria = Criteria.where("name").regex(pattern);
        query.addCriteria(criteria);
        return mongoTemplate.findAllAndRemove(query, KnowledgeTreeNode.class);
    }
}
