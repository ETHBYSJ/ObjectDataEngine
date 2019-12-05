package com.sjtu.objectdataengine.dao.tree;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.tree.RootMessage;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoRootDAO extends MongoBaseDAO<RootMessage> {

    public void addNewRoot(String id, String name, Date date) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is("root");
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("roots." + id , name);
        update.set("updateTime", date);
        mongoTemplate.updateMulti(query, update, RootMessage.class);
    }

    public void deleteRoot(String id, Date date) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is("root");
        query.addCriteria(criteria);
        Update update = new Update();
        update.unset("roots." + id);
        update.set("updateTime", date);
        mongoTemplate.updateMulti(query, update, RootMessage.class);
    }
}
