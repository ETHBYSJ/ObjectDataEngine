package com.sjtu.objectdataengine.utils;

import java.util.Objects;

public class SubscriberWrapper {
    // 订阅者id
    private String user;
    // 订阅类型
    private String subType;

    public SubscriberWrapper(String user, String subType) {
        this.user = user;
        this.subType = subType;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null) {
            return false;
        }
        if(obj.getClass() != this.getClass()){
            return false;
        }
        SubscriberWrapper subscriberWrapper = (SubscriberWrapper) obj;
        if(this.user.equals(subscriberWrapper.getUser()) && this.subType.equals(subscriberWrapper.getSubType())) {
            return true;
        }
        else return false;
    }
    @Override
    public int hashCode() {
        return Objects.hash(user, subType);
    }
}
