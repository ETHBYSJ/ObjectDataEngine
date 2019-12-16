package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Document(collection = "subscribeMessage")
public class EntityBaseSubscribeMessage extends BaseSubscribeMessage {
    private HashMap<String, List<String>> attrsSubscriber;   // attributes subscriber list
    private List<String> objectSubscriber;                   // object subscriber list

    public EntityBaseSubscribeMessage(String id, String type) {
        this.id = id + type;
        this.type = type;
        this.attrsSubscriber = new HashMap<>();
        this.objectSubscriber = new ArrayList<>();
    }

    public HashMap<String, List<String>> getAttrsSubscriber() {
        return attrsSubscriber;
    }

    public void setAttrsSubscriber(HashMap<String, List<String>> attrsSubscriber) {
        this.attrsSubscriber = attrsSubscriber;
    }

    public List<String> getObjectSubscriber() {
        return objectSubscriber;
    }

    public void setObjectSubscriber(List<String> objectSubscriber) {
        this.objectSubscriber = objectSubscriber;
    }
}
