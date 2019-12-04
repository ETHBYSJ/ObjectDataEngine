package com.sjtu.objectdataengine.utils;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

public class MongoCondition {

    private Query query;
    private Update update;

    public MongoCondition() {
        query = new Query();
        update = new Update();
    }

    public Query getQuery() {
        return query;
    }

    public void setQuery(Query query) {
        this.query = query;
    }

    public Update getUpdate() {
        return update;
    }

    public void setUpdate(Update update) {
        this.update = update;
    }

    public void whereIs(String attrName, Object value) {
        this.query.addCriteria(Criteria.where(attrName).is(value));
    }

    public void addSet(String setName, Object value) {
        this.update.addToSet(setName, value);
    }

    public void pull(String setName, Object value) {
        this.update.pull(setName, value);
    }

    public void unset(String key) {
        this.update.unset(key);
    }

    public void set(String key, String value) {
        this.update.set(key, value);
    }
}
