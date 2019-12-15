package com.sjtu.objectdataengine.model.subscribe;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Document(collection = "user")
public class User extends BaseModel {
    private String id;
    private String name;
    private String intro;
    private HashMap<String, List<String>> templateSubscribe; //templateId: [events]
    private List<String> objectSubscribe;
    // event -> template[]，用于直接索引到模板
    private HashMap<String, List<String>> inverseEvents;
    public HashMap<String, List<String>> getInverseEvents() {
        return this.inverseEvents;
    }
    public void setInverseEvents(HashMap<String, List<String>> inverseEvents) {
        this.inverseEvents = inverseEvents;
    }

    public User(String id, String name, String intro) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.objectSubscribe = new ArrayList<>();
        this.templateSubscribe = new HashMap<String, List<String>>();
    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }

    public List<String> getObjectSubscribe() {
        return objectSubscribe;
    }

    public void setObjectSubscribe(List<String> objectSubscribe) {
        this.objectSubscribe = objectSubscribe;
    }

    public HashMap<String, List<String>> getTemplateSubscribe() {
        return templateSubscribe;
    }

    public void setTemplateSubscribe(HashMap<String, List<String>> templateSubscribe) {
        this.templateSubscribe = templateSubscribe;
    }
}
