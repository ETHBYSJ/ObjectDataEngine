package com.sjtu.objectdataengine.model.object;

import com.sjtu.objectdataengine.model.MongoBase;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.Set;

@Document(collection = "mongoObjects")
public class CommonObject extends MongoBase {
    @Id
    private String id;              // 对象id
    private String name;            // 英文
    private String intro;           // 描述
    private String type;            // 类型
    private String template;        // 模板id

    private HashMap<String, Date> events; // 关联的events

    private HashMap<String, MongoAttr> attr;  // 最新属性集合

    public CommonObject(String id, String name, String intro, String type, String template, HashMap<String, MongoAttr> attr, HashMap<String, Date> events) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.type = type;
        this.template = template;
        this.attr = attr;
        this.events = events;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public HashMap<String, Date> getObjects() {
        return events;
    }

    public void setObjects(HashMap<String, Date> events) {
        this.events = events;
    }

    public HashMap<String, MongoAttr> getAttr() {
        return attr;
    }

    public void setAttr(HashMap<String, MongoAttr> attr) {
        this.attr = attr;
    }

    public void putAttr(String name, MongoAttr mongoAttr) {
        this.attr.put(name, mongoAttr);
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
}
