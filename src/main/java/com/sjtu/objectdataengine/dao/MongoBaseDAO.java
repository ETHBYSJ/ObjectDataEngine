package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.BaseModel;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;


import java.util.List;

public abstract class MongoBaseDAO<T extends BaseModel> {
    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 创建对象
     * @param t 存储对象
     * @return 布尔，1表示成功
     */
    public boolean create(T t) {
        try {
            mongoTemplate.insert(t);
            return true;
        } catch (DuplicateKeyException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 查询全部
     * @return List类型，返回集合所有数据
     */
    public List<T> findAll(Class<T> tClass) {
        return mongoTemplate.findAll(tClass);
    }

    /**
     * 根据主键id查询
     * @param id 主键id
     * @return T类型，返回某条数据
     */
    public T findById(String id, Class<T> tClass) {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(id));
        return mongoTemplate.findOne(query, tClass);
    }

    /**
     * 根据其他关键字查询
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    public List<T> findByArgs(MongoCondition mongoCondition, Class<T> tClass) {
        Query query = mongoCondition.getQuery();
        return mongoTemplate.find(query, tClass);
    }

    /**
     * 更新对象
     * @param mongoCondition 更新条件
     * @return true表示更新成功的数量>0
     */
    public boolean update(MongoCondition mongoCondition, Class<T> tClass) {
        Query query = mongoCondition.getQuery();
        Update update = mongoCondition.getUpdate();
        return mongoTemplate.updateMulti(query, update, tClass).getModifiedCount() > 0;
    }

    /**
     * 根据ID删除对象
     * @param id id
     * @return true表示删除成功的数量>0
     */
    public boolean deleteById(String id, Class<T> tClass) {
        T t = findById(id, tClass);
        return mongoTemplate.remove(t).getDeletedCount() > 0;
    }

    /**
     * 根据查询条件删除对象
     * @param mongoCondition 删除条件
     */
    public boolean deleteByArgs(MongoCondition mongoCondition, Class<T> tClass) {
        try {
            List<T> ts = findByArgs(mongoCondition, tClass);
            for (T t : ts) {
                mongoTemplate.remove(t);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
