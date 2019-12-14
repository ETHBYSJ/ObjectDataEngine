package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(collection = "subscribeMessage")
public class EntitySubscribeMessage extends SubscribeMessage {
    private HashMap<String, List<String>> attrsSubscriber;   // attributes subscriber list

    public EntitySubscribeMessage(String id, String type) {
        super(id, type);
    }

    public HashMap<String, List<String>> getAttrsSubscriber() {
        return attrsSubscriber;
    }

    public void setAttrsSubscriber(HashMap<String, List<String>> attrsSubscriber) {
        this.attrsSubscriber = attrsSubscriber;
    }
}
