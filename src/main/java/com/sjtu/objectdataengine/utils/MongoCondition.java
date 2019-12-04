package com.sjtu.objectdataengine.utils;

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

    public void addSet() {

    }
}
