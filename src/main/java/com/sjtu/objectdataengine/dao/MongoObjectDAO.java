package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoObjectDAO extends MongoBaseDAO<MongoObject>{
    /**
     * 查询全部
     *
     * @return List类型，返回集合所有数据
     */
    @Override
    public List<MongoObject> findAll() {
        return mongoTemplate.findAll(MongoObject.class);
    }

    /**
     * 根据主键key查询
     *
     * @param key 主键key
     * @return T类型，返回某条数据
     */
    @Override
    public MongoObject findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("_id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, MongoObject.class);
    }

    /**
     * 根据其他关键字查询
     *
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<MongoObject> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    /**
     * 更新对象
     *
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, MongoObject.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 在一个属性里增加一个值，包括update time，用mongoAttr封装
     * @param key 属性id
     * @param mongoAttr 值
     */
    public void updateAttrList(String key, String name, MongoAttr mongoAttr) {
        try {
            Query query = new Query();
            Update update = new Update();
            Criteria criteria = Criteria.where("_id").is(key);
            query.addCriteria(criteria);

            //Query query = Query.query(new Criteria().andOperator(Criteria.where("id").is(viewTemplateId),Criteria.where("template").elemMatch(Criteria.where("id").is(templateId))));

            update.set("attr." + name + ".value", mongoAttr.getValue());
            update.set("attr." + name + ".updateTime", mongoAttr.getUpdateTime());
            update.set("updateTime", new Date());
            mongoTemplate.updateMulti(query, update, MongoObject.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 模糊查询
     *
     * @param search 查询条件
     */
    @Override
    public List<MongoObject> fuzzySearch(String search) {
        return null;
    }
}
