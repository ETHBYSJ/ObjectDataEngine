package com.sjtu.objectdataengine.model.object;

import com.sjtu.objectdataengine.model.MongoBase;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Document(collection = "eventObjects")
public class EventObject extends MongoBase {
    @Id
    private String id;          // id
    private String name;        // 英文
    private String intro;       // 中文
    private String template;    // 模板id

    private List<String> objects;

    private HashMap<String, MongoAttr> attrs;

    private Date startTime;
    private Date endTime;

    private boolean status;      // 1-进行中 0-已结束
    private String stage;       // 自定义的事件子状态

    public EventObject(String id, String name, String intro, String template, List<String> objects, HashMap<String, MongoAttr> attrs) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.template = template;
        this.objects = objects;
        this.attrs = attrs;
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public HashMap<String, MongoAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(HashMap<String, MongoAttr> attr) {
        this.attrs = attrs;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
}
