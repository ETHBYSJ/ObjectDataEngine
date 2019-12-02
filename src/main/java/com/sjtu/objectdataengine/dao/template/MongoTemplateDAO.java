package com.sjtu.objectdataengine.dao.template;

import com.mongodb.client.result.UpdateResult;
import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class MongoTemplateDAO extends MongoBaseDAO<ObjectTemplate> {
    /**
     * 查询全部
     * @return String类型List，返回集合所有数据
     */
    @Override
    public List<ObjectTemplate> findAll() {
        return mongoTemplate.findAll(ObjectTemplate.class);
    }

    /**
     * 根据主键key查询
     * @param key 主键key
     * @return ObjectTemplate类型，返回某条数据
     */
    @Override
    public ObjectTemplate findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, ObjectTemplate.class);
    }

    /**
     * 根据其他关键字查询
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<ObjectTemplate> findByArgs(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        return mongoTemplate.find(query, ObjectTemplate.class);
    }

    /**
     * 更新对象
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, ObjectTemplate.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 模糊查询
     * @param search 查询条件
     * @return 查询结果
     */
    @Override
    public List<ObjectTemplate> fuzzySearch(String search) {
        Query query = new Query();
        Pattern pattern = Pattern.compile("^.*" + search + ".*$" , Pattern.CASE_INSENSITIVE);
        Criteria criteria = Criteria.where("name").regex(pattern);
        query.addCriteria(criteria);
        return mongoTemplate.findAllAndRemove(query, ObjectTemplate.class);
    }

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
