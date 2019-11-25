package com.sjtu.objectdataengine.dao.tree;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MongoTreeDAO extends MongoBaseDAO<TreeNode> {

    @Override
    public List<TreeNode> findAll() {
        return mongoTemplate.findAll(TreeNode.class);
    }

    @Override
    public TreeNode findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, TreeNode.class);
    }

    @Override
    public List<TreeNode> findByArgs(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        return mongoTemplate.find(query, TreeNode.class);
    }

    @Override
    public boolean update(MongoCondition mongoCondition) {
       try {
            Query query = mongoCondition.getQuery();
            Update update = mongoCondition.getUpdate();
            update.set("updateTime", new Date());
            mongoTemplate.updateMulti(query, update, TreeNode.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public List<TreeNode> fuzzySearch(String search) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + search + ".*$" , Pattern.CASE_INSENSITIVE);
        Criteria criteria = Criteria.where("name").regex(pattern);
        query.addCriteria(criteria);
        return mongoTemplate.findAllAndRemove(query, TreeNode.class);
    }

    public void addNewObject(String nodeId, String obj, String intro) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(nodeId);
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("objects." + obj, intro);
        mongoTemplate.updateMulti(query, update, TreeNode.class);
    }
}
