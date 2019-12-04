package com.sjtu.objectdataengine.dao.tree;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.utils.MongoConditionn;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

@Component
public class MongoTreeDAO extends MongoBaseDAO<TreeNode> {

    public void addNewObject(String nodeId, String obj, String intro) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(nodeId);
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("objects." + obj, intro);
        mongoTemplate.updateMulti(query, update, TreeNode.class);
    }
}
