package com.sjtu.objectdataengine.model;

import java.util.Date;

public class MongoAttr {
    private String value;
    private Date updateTime;

    MongoAttr(String value, Date updateTime) {
        this.value = value;
        this.updateTime = updateTime;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
