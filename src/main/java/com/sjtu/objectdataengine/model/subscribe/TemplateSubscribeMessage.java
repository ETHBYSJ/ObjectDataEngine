package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(collection = "subscribeMessage")
public class TemplateSubscribeMessage extends SubscribeMessage {
    public TemplateSubscribeMessage(String id, String type) {
        super(id, type);
    }
}
