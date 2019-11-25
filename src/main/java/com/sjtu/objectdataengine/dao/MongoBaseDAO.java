package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.model.MongoBase;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;


import java.util.Date;
import java.util.List;

public abstract class MongoBaseDAO<T extends MongoBase> {
    @Autowired
    protected MongoTemplate mongoTemplate;

    /**
     * 创建对象
     * @param t 存储对象
     * @return 布尔，1表示成功
     */
    public boolean create(T t) {
        Date date = new Date();
        t.setCreateTime(date);
        t.setUpdateTime(date);
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
    public abstract List<T> findAll();

    /**
     * 根据主键key查询
     * @param key 主键key
     * @return T类型，返回某条数据
     */
    public abstract T findByKey(String key);

    /**
     * 根据其他关键字查询
     * @param mongoCondition 查询条件
     * @return List类型，返回查询到的所有数据
     */
    public abstract List<T> findByArgs(MongoCondition mongoCondition);

    /**
     * 更新对象
     * @param mongoCondition 更新条件
     */
    public abstract boolean update(MongoCondition mongoCondition);

    /**
     * 根据键值删除对象
     * @param key 键
     */
    public boolean deleteByKey(String key) {
        T t = findByKey(key);
        try {
            mongoTemplate.remove(t);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 根据查询条件删除对象
     * @param mongoCondition 删除条件
     */
    public boolean deleteByArgs(MongoCondition mongoCondition) {
        try {
            List<T> ts = findByArgs(mongoCondition);
            for (T t : ts) {
                mongoTemplate.remove(t);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 模糊查询
     * @param search 查询条件
     */
    public abstract List<T> fuzzySearch(String search);

}
