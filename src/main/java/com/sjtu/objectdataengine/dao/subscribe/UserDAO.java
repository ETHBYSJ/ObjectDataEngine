package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.MongoSequence;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@Component
public class UserDAO extends MongoBaseDAO<User> {
    /*
    public Long getNextId(String collectionName) {
        MongoSequence seq = mongoTemplate.findAndModify(
                query(where("_id").is(collectionName)),
                new Update().inc("seq", 1),
                options().upsert(true).returnNew(true),
                MongoSequence.class);
        return seq.getSeq();
    }
    */
    /**
     * 是否含有该user Id
     * @param id userId
     * @return true or false
     */
    public boolean hasUser(String id) {
        return findById(id, User.class) != null;
    }

    /**
     * 是否含有该user Name
     * @param userName userName
     * @return true or false
     */
    public boolean hasUserName(String userName) {
        Query query = new Query();
        query.addCriteria(where("name").is(userName));
        return mongoTemplate.findOne(query, User.class) != null;
    }

    /**
     * 增加一个针对对象的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param name 属性
     */
    public void addObjectSubscribe(String userId, String objId, String name) {
        String key = name==null ? objId : objId + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("objectSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }

    /**
     * 删除一个针对对象的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param name 属性
     */
    public void delObjectSubscribe(String userId, String objId, String name) {
        String key = name==null ? objId : objId + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pull("objectSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 增加一个针对事件的订阅
     * @param userId 用户id
     * @param eventId 事件id
     * @param name 属性
     */
    public void addEventSubscribe(String userId, String eventId, String name) {
        String key = name==null ? eventId : eventId + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("eventSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 删除一个针对事件的订阅
     * @param userId 用户id
     * @param eventId 事件id
     * @param name 属性
     */
    public void delEventSubscribe(String userId, String eventId, String name) {
        String key = name==null ? eventId : eventId + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pull("eventSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 增加一个针对模板的订阅
     * @param userId 用户id
     * @param template 模板id
     * @param name 属性
     */
    public void addTemplateSubscribe(String userId, String template, String name) {
        String key = name==null ? template : template + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("templateSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 删除一个针对模板的订阅
     * @param userId 用户id
     * @param template 模板id
     * @param name 属性
     */
    public void delTemplateSubscribe(String userId, String template, String name) {
        String key = name==null ? template : template + ":" + name;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pull("templateSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 重载，实现添加整个对象订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addObjectSubscribe(String userId, String objId) {
        this.addObjectSubscribe(userId, objId, null);
    }
    public void delObjectSubscribe(String userId, String objId) {
        this.delObjectSubscribe(userId, objId, null);
    }
    /**
     * 重载，实现添加整个事件订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addEventSubscribe(String userId, String objId) {
        this.addEventSubscribe(userId, objId, null);
    }
    public void delEventSubscribe(String userId, String objId) {
        this.delEventSubscribe(userId, objId, null);
    }
    /**
     * 重载，实现添加整个模板订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addTemplateSubscribe(String userId, String objId) {
        this.addTemplateSubscribe(userId, objId, null);
    }
    public void delTemplateSubscribe(String userId, String objId) {
        this.delTemplateSubscribe(userId, objId, null);
    }
}
