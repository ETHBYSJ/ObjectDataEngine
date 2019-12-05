package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.object.AttrsModel;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MongoAttrsDAO extends MongoBaseDAO<AttrsModel> {
    /**
     * 在一个属性里增加一个值，包括update time，用mongoAttr封装
     * @param key 属性id
     * @param size 属性长度
     * @param mongoAttr 值
     * @param date 公共date
     * @return true or false
     */
    public boolean addValue(String key, int size, MongoAttr mongoAttr, Date date) {
        try {
            Query query = new Query();
            Update update = new Update();
            Criteria criteria = Criteria.where("_id").is(key);
            query.addCriteria(criteria);
            update.addToSet("attrs", mongoAttr);
            update.set("size", size + 1);
            update.set("updateTime", date);
            mongoTemplate.updateMulti(query, update, AttrsModel.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
