package com.sjtu.objectdataengine.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MongoCondition {
    private String opType;                  //操作类型，query和update和delete
    private Map<String, String> queryMap;
    private Map<String, String> updateMap;

    public MongoCondition() {
        this.queryMap = new HashMap<String, String>();
        this.updateMap = new HashMap<String, String>();
    }

    public MongoCondition(String opType, Map<String, String> queryMap, Map<String, String> updateMap) {
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

    public Map<String, String> getQueryMap() {
        return queryMap;
    }

    public void setQueryMap(Map<String, String> queryMap) {
        this.queryMap = queryMap;
    }

    public Map<String, String> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, String> updateMap) {
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

    public void addQuery(String key, String value) {
        this.queryMap.put(key, value);
    }

    public void addUpdate(String key, String value) {
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

    @Override
    public String toString() {
        return queryMap.toString();
    }
}
