package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.MongoBaseDAO;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.object.EventObject;
import com.sjtu.objectdataengine.utils.MongoAttr;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class MongoEventDAO extends MongoBaseDAO<EventObject> {
    /**
     * 查询全部
     *
     * @return List类型，返回集合所有数据
     */
    @Override
    public List<EventObject> findAll() {
        return mongoTemplate.findAll(EventObject.class);
    }

    /**
     * 根据主键key查询
     *
     * @param key 主键key
     * @return EventObject类型，返回某条数据
     */
    @Override
    public EventObject findByKey(String key) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is(key);
        query.addCriteria(criteria);
        return mongoTemplate.findOne(query, EventObject.class);
    }

    /**
     * 根据其他关键字查询
     *
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<EventObject> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    /**
     * 更新对象
     * 允许包括name, intro, stage
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        update.set("updateTime", new Date());
        try {
            mongoTemplate.updateMulti(query, update, EventObject.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

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
    /**
     * 模糊查询
     *
     * @param search 查询条件
     */
    @Override
    public List<EventObject> fuzzySearch(String search) {
        return null;
    }
}
