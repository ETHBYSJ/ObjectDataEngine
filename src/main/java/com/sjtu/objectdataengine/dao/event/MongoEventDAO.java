package com.sjtu.objectdataengine.dao.event;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.object.MongoAttr;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;


@Component
public class MongoEventDAO extends MongoBaseDAO<EventObject> {
    /**
     * 更新事件属性信息
     * @param id 事件id
     * @param name 属性名（英文）
     * @param mongoAttr 属性值封装
     */
    public void opValue(String id, String name, MongoAttr mongoAttr) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(id);
        query.addCriteria(criteria);

        Update update = new Update();
        update.set("attrs." + name, mongoAttr);

        mongoTemplate.updateMulti(query, update, EventObject.class);
    }

    /**
     * 更新事件关联信息
     * @param op add或del,增加删除
     */
    public void opObjects(String id, String objId, String op) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(id);
        query.addCriteria(criteria);

        Update update = new Update();

        if (op.equals("add")) {
            update.addToSet("objects", objId);
        } else if (op.equals("del")) {
            update.pull("objects", objId);
        } else return;

        mongoTemplate.updateMulti(query, update, EventObject.class);
    }
}
