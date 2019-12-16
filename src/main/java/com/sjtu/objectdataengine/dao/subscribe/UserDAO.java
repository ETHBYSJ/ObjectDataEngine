package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class UserDAO extends MongoBaseDAO<User> {

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
     * 针对单条属性的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param attr 属性名
     */
    public void addAttrSubscribe(String userId, String objId, String attr) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("attrSubscribe." + objId, attr);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 删除属性的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param attr 属性名
     */
    public void delAttrSubscribe(String userId, String objId, String attr) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pull("attrSubscribe." + objId, attr);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 针对属性的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param attrs 属性列表
     */
    public void addAttrSubscribe(String userId, String objId, List<String> attrs) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("attrsSubscribe." + objId).each(attrs.toArray());
        mongoTemplate.updateMulti(query, update, User.class);
    }

    /**
     * 删除属性的订阅
     * @param userId 用户id
     * @param objId 对象id
     * @param attrs 属性列表
     */
    public void delAttrSubscribe(String userId, String objId, List<String> attrs) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();

        for(String attr : attrs) {
            update.pull("attrsSubscribe." + objId, attr);
        }
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 增加一个针对对象的订阅
     * @param userId 用户id
     * @param objId 对象id
     */
    public void addObjectSubscribe(String userId, String objId) {
        String key = objId;
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
     */
    public void delObjectSubscribe(String userId, String objId) {
        String key = objId;
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.pull("objectSubscribe", key);
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 增加一个针对模板的订阅
     * @param userId 用户id
     * @param template 模板id
     * @param events 关联对象/事件表
     */
    public void addTemplateSubscribe(String userId, String template, List<String> events) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("templateSubscribe." + template).each(events.toArray());
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 删除一个针对模板的订阅
     * @param userId 用户id
     * @param template 模板id
     */
    public void delTemplateSubscribe(String userId, String template) {
        User user = this.findById(userId, User.class);
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.unset("templateSubscribe." + template);
        mongoTemplate.updateMulti(query, update, User.class);
    }

}
