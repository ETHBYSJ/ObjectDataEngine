package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.object.MongoAttr;
import com.sjtu.objectdataengine.model.object.CommonObject;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MongoObjectDAO extends MongoBaseDAO<CommonObject> {
    /**
     * 在一个属性里增加一个值，包括update time，用mongoAttr封装
     * @param key 属性id
     * @param mongoAttr 值
     */
    public void updateAttrList(String key, String name, MongoAttr mongoAttr, Date date) {
        try {
            Query query = new Query();
            Update update = new Update();
            Criteria criteria = Criteria.where("_id").is(key);
            query.addCriteria(criteria);

            //Query query = Query.query(new Criteria().andOperator(Criteria.where("id").is(viewTemplateId),Criteria.where("template").elemMatch(Criteria.where("id").is(templateId))));

            update.set("attr." + name + ".value", mongoAttr.getValue());
            update.set("attr." + name + ".updateTime", mongoAttr.getUpdateTime());
            update.set("updateTime", date);
            mongoTemplate.updateMulti(query, update, CommonObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除一个event
     *
     * @param id 查询条件
     */
    public void delEvent(String id, String eventId, Date date) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        Update update = new Update();
        update.unset("events." + eventId);
        update.set("updateTime", date);
        mongoTemplate.updateMulti(query, update, CommonObject.class);
    }
}
