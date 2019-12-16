package com.sjtu.objectdataengine.model.object;

import java.util.Date;

public class MongoAttr {

    private String value;
    private Date updateTime;

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

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public boolean equals(Object object) {
        if(this == object){
            return true;//地址相等
        }

        if(object == null){
            return false;//非空性：对于任意非空引用x，x.equals(null)应该返回false。
        }
        if (object instanceof MongoAttr) {
            MongoAttr obj = (MongoAttr) object;
            return (this.value.equals(obj.value) && this.getUpdateTime().equals(obj.getUpdateTime()));
        }
        return false;
    }
}
