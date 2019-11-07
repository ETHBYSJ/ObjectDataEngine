package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.RootMessage;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MongoRootDAO extends MongoBaseDAO<RootMessage>{

    /**
     * 查询全部
     *
     * @return List类型，返回集合所有数据
     */
    @Override
    public List<RootMessage> findAll() {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is("root");
        query.addCriteria(criteria);
        return mongoTemplate.find(query, RootMessage.class);
    }

    /**
     * 根据主键key查询
     *
     * @param key 主键key
     * @return T类型，返回某条数据
     */
    @Override
    public RootMessage findByKey(String key) {
        return null;
    }

    /**
     * 根据其他关键字查询
     *
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    @Override
    public List<RootMessage> findByArgs(MongoCondition mongoCondition) {
        return null;
    }

    /**
     * 更新对象
     *
     * @param mongoCondition 更新条件
     */
    @Override
    public boolean update(MongoCondition mongoCondition) {
        return false;
    }

    /**
     * 模糊查询
     *
     * @param search 查询条件
     */
    @Override
    public List<RootMessage> fuzzySearch(String search) {
        return null;
    }

    public void addNewRoot(String id, String name) {
        Query query = new Query();
        Criteria criteria = Criteria.where("id").is("root");
        query.addCriteria(criteria);
        Update update = new Update();
        update.set("roots." + id , name);
        mongoTemplate.updateMulti(query, update, RootMessage.class);
    }
}
