package com.sjtu.objectdataengine.model.object;

import com.alibaba.fastjson.JSONObject;
import com.mongodb.Mongo;
import com.sjtu.objectdataengine.model.BaseModel;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Document(collection = "mongoObjects")
public class CommonObject extends BaseModel {
    @Id
    private String id;              // 对象id
    private String name;            // 英文
    private String intro;           // 描述
    private String type;            // 类型
    private String template;        // 模板id

    private HashMap<String, Date> events; // 关联的events

    private HashMap<String, MongoAttr> attrs;  // 最新属性集合

    public CommonObject(String id, String name, String intro, String type, String template, HashMap<String, MongoAttr> attrs, HashMap<String, Date> events) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.type = type;
        this.template = template;
        this.attrs = attrs;
        this.events = events;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public HashMap<String, Date> getEvents() {
        return events;
    }

    public void setEvents(HashMap<String, Date> events) {
        this.events = events;
    }

    public HashMap<String, MongoAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(HashMap<String, MongoAttr> attr) {
        this.attrs = attr;
    }

    public void putAttrs(String name, MongoAttr mongoAttr) {
        this.attrs.put(name, mongoAttr);
    }

    /**
     * 清扫time之后的关联，用于返回某个时间点的对象
     * @param time 截止时间
     */
    public void cutObjects(Date time) {
        if (this.events != null) {
            Set<String> keySet = this.events.keySet();
            for (String key : keySet) {
                Date bindTime = this.events.get(key);
                if (bindTime.after(time)) {
                    this.events.remove(key);
                }
            }
        }
    }


    @Override
    public String toString() {
        StringBuilder eventsStr = new StringBuilder("{");
        StringBuilder attrsStr = new StringBuilder("{");

        for (Map.Entry event : events.entrySet()) {
            eventsStr.append("\"").append(event.getKey()).append("\":").append(event.getValue()).append(",");
        }
        if (eventsStr.length() > 1) eventsStr.setCharAt(eventsStr.length()-1, '}');
        else eventsStr.append('}');

        for (Map.Entry attr : attrs.entrySet()) {
            MongoAttr mongoAttr = (MongoAttr) attr.getValue();
            attrsStr.append("\"").append(attr.getKey()).append("\":").append("{\"value\":").append(mongoAttr.getValue()).append(",\"updateTime\":")
                    .append(mongoAttr.getUpdateTime().toString()).append("},");
        }
        if (attrsStr.length() > 1) attrsStr.setCharAt(attrsStr.length()-1, '}');
        else attrsStr.append('}');

        return ("{\"id\":" + id +
                ",\"name\":" + name +
                ",\"intro\":" + intro +
                ",\"type\":" + type +
                ",\"template\":" + template +
                ",\"events\":" + eventsStr +
                ",\"attrs\":" + attrsStr +
                ",\"createTime\":" + getCreateTime().toString() +
                ",\"updateTime\":" + getUpdateTime().toString() +
                "}"
        );
    }
}
