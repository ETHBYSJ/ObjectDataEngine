package com.sjtu.objectdataengine.dao.template;

import com.mongodb.client.result.UpdateResult;
import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MongoTemplateDAO extends MongoBaseDAO<ObjectTemplate> {

    public void opAttr(String id, String name, String op, Date date) {
        if(op.equals("del")) {
            opAttr(id, name, null, op, date);
        }
    }

    public void opAttr(String id, String name, String nickname, String op, Date date) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(id);
        query.addCriteria(criteria);

        Update update = new Update();
        update.set("updateTime", date);
        if (op.equals("add")) {
            update.set("attrs." + name, nickname);
        } else if (op.equals("del")) {
            update.unset("attrs." + name);
        } else {
            return;
        }

        mongoTemplate.updateMulti(query, update, ObjectTemplate.class);
    }

    public boolean opObjects(String id, String objId, String op) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(id);
        query.addCriteria(criteria);

        Update update = new Update();
        if (op.equals("add")) {
            update.addToSet("objects", objId);
        } else if (op.equals("del")) {
            update.pull("objects" , objId);
        } else {
            return false;
        }

        UpdateResult updateRequest = mongoTemplate.updateMulti(query, update, ObjectTemplate.class);

        return (updateRequest.getMatchedCount() > 0 && updateRequest.getModifiedCount() > 0);
    }
}
