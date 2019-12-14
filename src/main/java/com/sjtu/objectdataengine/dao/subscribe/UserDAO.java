package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.MongoSequence;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

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
        update.addToSet("objectSubscribe", objId + ':' + attr);
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
        update.pull("objectSubscribe", objId + ':' + attr);
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
        List<String> keys = new ArrayList<>();

        for(String attr : attrs) {
            String key = objId + ':' + attr;
            keys.add(key);
        }
        update.addToSet("objectSubscribe").each(keys.toArray());
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
        List<String> keys = new ArrayList<>();

        for(String attr : attrs) {
            String key = objId + ':' + attr;
            keys.add(key);
        }
        update.pullAll("objectSubscribe", keys.toArray());
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
     * @param list 关联对象/事件表
     */
    public void addTemplateSubscribe(String userId, String template, List<String> list) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.addToSet("templateSubscribe." + template).each(list.toArray());
        mongoTemplate.updateMulti(query, update, User.class);
    }
    /**
     * 删除一个针对模板的订阅
     * @param userId 用户id
     * @param template 模板id
     */
    public void delTemplateSubscribe(String userId, String template) {
        Query query = new Query();
        query.addCriteria(where("_id").is(userId));
        Update update = new Update();
        update.unset("templateSubscribe." + template);
        mongoTemplate.updateMulti(query, update, User.class);
    }

}
