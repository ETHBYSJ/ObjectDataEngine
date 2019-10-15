package com.sjtu.objectdataengine.utils;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoCondition {
    private String opType;                  //操作类型，query和update和delete
    private Map<String, Object> queryMap;
    private Map<String, Object> updateMap;

    public MongoCondition() {
        this.queryMap = new HashMap<String, Object>();
        this.updateMap = new HashMap<String, Object>();
    }

    public MongoCondition(String opType, Map<String, Object> queryMap, Map<String, Object> updateMap) {
        this.opType = opType;
        this.queryMap = queryMap;
        this.updateMap = updateMap;
    }
    public String getOpType() {
        return opType;
    }

    public void setOpType(String opType) {
        this.opType = opType;
    }

    public Map<String, Object> getQueryMap() {
        return queryMap;
    }


    public Map<String, ?> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Object> updateMap) {
        this.updateMap = updateMap;
    }

    public boolean queryIsEmpty() {
        return this.queryMap.isEmpty();
    }

    public boolean updateIsEmpty() {
        return this.updateMap.isEmpty();
    }

    public int queryMapSize() {
        return queryMap.size();
    }

    public int updateMapSize() {
        return updateMap.size();
    }

    public void addQuery(String key, Object value) {
        this.queryMap.put(key, value);
    }

    public void addUpdate(String key, Object value) {
        this.updateMap.put(key, value);
    }

    public void delQuery(String key) {
        this.queryMap.remove(key);
    }

    public void delUpdate(String key) {
        this.updateMap.remove(key);
    }

    public void clearQuery() {
        this.queryMap.clear();
    }

    public void clearUpdate() {
        this.updateMap.clear();
    }

    public Query getQuery() {
        Query query = new Query();
        for(Map.Entry<String, Object> entry : queryMap.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            query.addCriteria(Criteria.where(mapKey).is(mapValue));
        }
        return query;
    }

    public Update getUpdate() {
        Update update = new Update();
        for(Map.Entry<String, Object> entry : updateMap.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = entry.getValue();
            update.set(mapKey, mapValue);
        }
        return update;
    }

    @Override
    public String toString() {
        return queryMap.toString();
    }
}
