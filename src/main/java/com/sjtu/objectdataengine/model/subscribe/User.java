package com.sjtu.objectdataengine.model.subscribe;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "user")
public class User extends BaseModel {
    private String id;
    private String name;
    private String intro;
    //private List<String> eventSubscribe;
    private List<String> objectSubscribe;
    private List<String> templateSubscribe;

    public User(String id, String name, String intro) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        //this.eventSubscribe = new ArrayList<>();
        this.objectSubscribe = new ArrayList<>();
        this.templateSubscribe = new ArrayList<>();
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
    /*
    public List<String> getEventSubscribe() {
        return eventSubscribe;
    }
    public void setEventSubscribe(List<String> eventSubscribe) {
        this.eventSubscribe = eventSubscribe;
    }
    */
    public List<String> getObjectSubscribe() {
        return objectSubscribe;
    }

    public void setObjectSubscribe(List<String> objectSubscribe) {
        this.objectSubscribe = objectSubscribe;
    }

    public List<String> getTemplateSubscribe() {
        return templateSubscribe;
    }

    public void setTemplateSubscribe(List<String> templateSubscribe) {
        this.templateSubscribe = templateSubscribe;
    }
}
