package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(collection = "subscribeMessage")
public class TemplateBaseSubscribeMessage extends BaseSubscribeMessage {

    private HashMap<String, List<String>> templateSubscriber;

    public TemplateBaseSubscribeMessage(String id, String type) {
        this.id = id + type;
        this.type = type;
        this.templateSubscriber = new HashMap<>();
    }

    public HashMap<String, List<String>> getTemplateSubscriber() {
        return templateSubscriber;
    }

    public void setTemplateSubscriber(HashMap<String, List<String>> templateSubscriber) {
        this.templateSubscriber = templateSubscriber;
    }

    @Override
    public String toString() {
        return "{\"id\":" + this.id +
                ",\"type\":" + this.type +
                ",\"templateSubscriber\":" + this.templateSubscriber + "}";
    }
}
