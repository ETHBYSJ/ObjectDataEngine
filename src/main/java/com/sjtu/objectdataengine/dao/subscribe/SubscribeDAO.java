package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.BaseSubscribeMessage;
import org.springframework.stereotype.Component;

@Component

public class SubscribeDAO extends MongoBaseDAO<BaseSubscribeMessage> {

    /**
     * 增加一个属性订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param name  属性名称
     * @param user  用户id
     */
    /*
    public boolean addAttrSubscriber(String objId, String type, String name, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.addToSet("attrsSubscriber." + name, user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
    /**
     * 删除一个属性订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param name  属性名称
     * @param user  用户id
     */
    /*
    public boolean delAttrSubscriber(String objId, String type, String name, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.pull("attrsSubscriber." + name, user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
    /**
     * 增加一个对象订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param user  用户id
     */
    /*
    public boolean addObjectSubscriber(String objId, String type, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.addToSet("objectSubscriber", user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
    /**
     * 删除一个对象订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param user  用户id
     */
    /*
    public boolean delObjectSubscriber(String objId, String type, String user) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.pull("objectSubscriber", user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
    /**
     * 增加一个属性
     * 这里注意，编辑事件和模板attrs列表的时候，要同步到这边
     * @param id   object id
     * @param type 类型
     * @param attr 属性名称
     */
    /*
    public boolean addAttr(String id, String type, String attr) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id + type));
        Update update = new Update();
        update.set("attrsSubscriber." + attr, new ArrayList<>());
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
    /**
     * 删除一个属性
     * 这里注意，编辑事件和模板attrs列表的时候，要同步到这边
     * @param id   object id
     * @param type 类型
     * @param attr 属性名称
     */
    /*
    public boolean delAttr(String id, String type, String attr) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id + type));
        Update update = new Update();
        update.unset("attrsSubscriber." + attr);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }

    private boolean addObj(String id, String type) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id + type));
        Update update = new Update();
        update.set("objectSubscriber", new ArrayList<String>());
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, BaseSubscribeMessage.class).getModifiedCount() > 0;
    }
    */
}