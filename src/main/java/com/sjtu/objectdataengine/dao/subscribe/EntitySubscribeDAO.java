package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.EntityBaseSubscribeMessage;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class EntitySubscribeDAO extends MongoBaseDAO<EntityBaseSubscribeMessage> {
    /**
     * 增加属性订阅者
     * @param objId 对象id
     * @param type 类型(entity)
     * @param attrs 属性列表
     * @param user 用户id
     * @return true or false
     */
    public boolean addAttrSubscriber(String objId, String type, List<String> attrs, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        for(String attr : attrs) {
            update.addToSet("attrsSubscriber." + attr, user);
        }
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }

    /**
     *
     * @param objId 对象id
     * @param type 类型(entity)
     * @param attrs 属性列表
     * @param user 用户id
     * @return true or false
     */
    public boolean delAttrSubscriber(String objId, String type, List<String> attrs, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        for(String attr : attrs) {
            update.pull("attrsSubscriber." + attr, user);
        }
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    /**
     * 增加一个属性订阅者
     * @param objId 对象id
     * @param type  对象类型
     * @param name  属性名称
     * @param user  用户id
     */
    public boolean addAttrSubscriber(String objId, String type, String name, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.addToSet("attrsSubscriber." + name, user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }

    /**
     * 删除一个属性订阅者
     * @param objId 对象id
     * @param type  对象类型
     * @param name  属性名称
     * @param user  用户id
     */
    public boolean delAttrSubscriber(String objId, String type, String name, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.pull("attrsSubscriber." + name, user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }


    /**
     * 增加一个对象订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param user  用户id
     */
    public boolean addObjectSubscriber(String objId, String type, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.addToSet("objectSubscriber", user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    /**
     * 删除一个对象订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param user  用户id
     */
    public boolean delObjectSubscriber(String objId, String type, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.pull("objectSubscriber", user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, EntityBaseSubscribeMessage.class).getModifiedCount() > 0;
    }

}
