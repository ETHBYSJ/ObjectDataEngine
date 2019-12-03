package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserDAO extends MongoBaseDAO<User> {
    /**
     * 查询全部
     *
     * @return List类型，返回集合所有数据
     */
    @Override
    public List<User> findAll() {
        return mongoTemplate.findAll(User.class);
    }

    /**
     * 根据主键key查询
     *
     * @param key 主键key
     * @return T类型，返回某条数据
     */
    @Override
    public User findByKey(String key) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(key));
        return mongoTemplate.findOne(query, User.class);
    }

    /**
     * 根据其他关键字查询
     *
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<User> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    /**
     * 更新对象
     *
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
        return false;
    }

    /**
     * 模糊查询
     *
     * @param search 查询条件
     */
    @Override
    public List<User> fuzzySearch(String search) {
        return null;
    }

    /**
     * 是否含有该user Id
     * @param id userId
     * @return true or false
     */
    public boolean hasUser(String id) {
        return findByKey(id) == null;
    }

    /**
     * 是否含有该user Name
     * @param userName userName
     * @return true or false
     */
    public boolean hasUserName(String userName) {
        Query query = new Query();
        query.addCriteria(Criteria.where("name").is(userName));
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
        query.addCriteria(Criteria.where("_id").is(userId));
        Update update = new Update();
        update.addToSet("objectSubscribe", key);
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
        query.addCriteria(Criteria.where("_id").is(userId));
        Update update = new Update();
        update.addToSet("eventSubscribe", key);
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
        query.addCriteria(Criteria.where("_id").is(userId));
        Update update = new Update();
        update.addToSet("templateSubscribe", key);
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

    /**
     * 重载，实现添加整个事件订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addEventSubscribe(String userId, String objId) {
        this.addEventSubscribe(userId, objId, null);
    }

    /**
     * 重载，实现添加整个模板订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addTemplateSubscribe(String userId, String objId) {
        this.addTemplateSubscribe(userId, objId, null);
    }
}
