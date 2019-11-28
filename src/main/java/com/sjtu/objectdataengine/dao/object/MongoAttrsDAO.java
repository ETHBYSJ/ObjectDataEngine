package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.object.MongoAttrs;
import com.sjtu.objectdataengine.utils.MongoAttr;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoAttrsDAO extends MongoBaseDAO<MongoAttrs> {

    /**
     * 查找全部属性
     * @return list
     */
    @Override
    public List<MongoAttrs> findAll() {
        return mongoTemplate.findAll(MongoAttrs.class);
    }

    /**
     * 根据主键id查询某条属性
     * @param key 主键key
     * @return 对象
     */
    @Override
    public MongoAttrs findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        //System.out.println(query);
        return mongoTemplate.findOne(query, MongoAttrs.class);
    }

    @Override
    public List<MongoAttrs> findByArgs(MongoCondition mongoCondition) {
        return null;
    }



    @Override
    public boolean update(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, MongoAttrs.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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
            mongoTemplate.updateMulti(query, update, MongoAttrs.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<MongoAttrs> fuzzySearch(String search) {
        return null;
    }





}
