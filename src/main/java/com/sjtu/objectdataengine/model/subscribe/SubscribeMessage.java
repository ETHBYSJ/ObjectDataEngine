package com.sjtu.objectdataengine.model.subscribe;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(collection = "subscribeMessage")
public class SubscribeMessage extends BaseModel {
    @Id
    private String id;          // object Id + type
    private String type;        // type: event or entity

    private HashMap<String, List<String>> subscribeList;

    public SubscribeMessage(String id, String type, HashMap<String, List<String>> subscribeList) {
        this.id = id + type;
        this.type = type;
        this.subscribeList = subscribeList;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public HashMap<String, List<String>> getSubscribeList() {
        return subscribeList;
    }

    public void setSubscribeList(HashMap<String, List<String>> subscribeList) {
        this.subscribeList = subscribeList;
    }
}
