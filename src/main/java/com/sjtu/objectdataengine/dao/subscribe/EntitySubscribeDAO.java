package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.EntitySubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class EntitySubscribeDAO extends MongoBaseDAO<EntitySubscribeMessage> {
    /**
     * 增加一个属性订阅者
     *
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
        return mongoTemplate.updateMulti(query, update, EntitySubscribeMessage.class).getModifiedCount() > 0;
    }

    /**
     * 删除一个属性订阅者
     *
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
        return mongoTemplate.updateMulti(query, update, EntitySubscribeMessage.class).getModifiedCount() > 0;
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
        return mongoTemplate.updateMulti(query, update, EntitySubscribeMessage.class).getModifiedCount() > 0;
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
        return mongoTemplate.updateMulti(query, update, EntitySubscribeMessage.class).getModifiedCount() > 0;
    }

}
