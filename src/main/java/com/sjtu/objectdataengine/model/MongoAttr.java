package com.sjtu.objectdataengine.model;

import java.util.Date;

public class MongoAttr extends MongoBase{

    private String value;

    public MongoAttr(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value + " " + getUpdateTime();
    }
}
