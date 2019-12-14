package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "subscribeMessage")
public class TemplateSubscribeMessage extends SubscribeMessage {
    public TemplateSubscribeMessage(String id, String type) {
        super(id, type);
    }
}
