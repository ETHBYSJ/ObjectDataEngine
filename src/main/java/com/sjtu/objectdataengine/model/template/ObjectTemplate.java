package com.sjtu.objectdataengine.model.template;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.List;

@Document(collection="objectTemplate")
public class ObjectTemplate extends BaseModel {
    @Id
    private String id;                          // 对象模板id
    private String name;                        // 对象模板name
    private String intro;                       // 说明
    private String nodeId;                      // 对应的结点id
    private String type;                        // 类型，有事件和实体
    private List<String> objects;               // 关联对象
    private HashMap<String, String> attrs;      // 属性集合

    public ObjectTemplate(String id, String name, String intro, String nodeId, String type, HashMap<String, String> attrs, List<String> objects) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.attrs = attrs;
        this.nodeId = nodeId;
        this.type = type;
        this.objects = objects;
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

    public HashMap<String, String> getAttrs() {
        return attrs;
    }

    public void setAttrs(HashMap<String, String> attribute) {
        this.attrs = attribute;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addAttr(String name, String nickname) {
        this.attrs.put(name, nickname);
    }

    public void delAttr(String name) {
        this.attrs.remove(name);
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }

    public void putObject(String key) {
        this.objects.add(key);
    }

    public void removeObject(String key) {
        this.objects.remove(key);
    }

    @Override
    public String toString() {
        return id + " " + name + " " + attrs;
    }

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
    }
}
