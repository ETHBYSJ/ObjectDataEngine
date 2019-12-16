package com.sjtu.objectdataengine.dao.subscribe;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.subscribe.TemplateBaseSubscribeMessage;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class TemplateSubscribeDAO extends MongoBaseDAO<TemplateBaseSubscribeMessage> {
    /**
     * 增加一个对象订阅者
     *
     * @param objId 对象id
     * @param type  对象类型
     * @param user  用户id
     */
    public boolean addObjectSubscriber(String objId, String type, String user, List<String> events) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(objId + type));
        Update update = new Update();
        update.set("templateSubscriber." + user, events);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, TemplateBaseSubscribeMessage.class).getModifiedCount() > 0;
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
        update.pull("templateSubscriber", user);
        update.set("updateTime", new Date());
        return mongoTemplate.updateMulti(query, update, TemplateBaseSubscribeMessage.class).getModifiedCount() > 0;
    }

}
