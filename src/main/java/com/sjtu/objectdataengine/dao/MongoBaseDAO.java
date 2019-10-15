package com.sjtu.objectdataengine.dao;

import com.sjtu.objectdataengine.utils.MongoCondition;

import java.util.List;

public abstract class MongoBaseDAO<T> {


    /**
     * 创建对象
     * @param t 存储对象
     * @return 布尔，1表示成功
     */
    public abstract boolean create(T t);

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
    public abstract boolean deleteByKey(String key);

    /**
     * 根据查询条件删除对象
     * @param mongoCondition 删除条件
     */
    public abstract boolean deleteByArgs(MongoCondition mongoCondition);

    /**
     * 模糊查询
     * @param search 查询条件
     */
    public abstract List<T> fuzzySearch(String search);
}
