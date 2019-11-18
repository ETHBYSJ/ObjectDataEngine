package com.sjtu.objectdataengine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Set;

@Document(collection="objectTemplate")
public class ObjectTemplate extends MongoBase{
    @Id
    private String id;              //对象模板id
    private String name;            //对象模板name
    private Set<String> attrs;       //属性集合
    private String nodeId;          //对应的结点id
    private String type;            //类型，有事件和实体
    private HashMap<String, String> objects;

    public ObjectTemplate(String id, String name, Set<String> attrs, String nodeId, String type/*, HashMap<String, String> objects*/) {
        this.id = id;
        this.name = name;
        this.attrs = attrs;
        this.nodeId = nodeId;
        this.type = type;
        //this.objects = objects;
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

    public Set<String> getAttrs() {
        return attrs;
    }

    public void setAttrs(Set<String> attribute) {
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

    public boolean addAttr(String name) {
        return this.attrs.add(name);
    }

    public boolean delAttr(String name) {
        return this.attrs.remove(name);
    }

    public boolean replaceAttr(String oldName, String newName) {
        if (this.attrs.remove(oldName)) {
            return this.attrs.add(newName);
        } else {
            return false;
        }
    }

    public HashMap<String, String> getObjects() {
        return objects;
    }

    public void setObjects(HashMap<String, String> objects) {
        this.objects = objects;
    }

    public void putObject(String key, String name) {
        this.objects.put(key, name);
    }

    public void removeObject(String key) {
        this.objects.remove(key);
    }

    public void getObject(String key) {
        this.objects.get(key);
    }
    @Override
    public String toString() {
        return id + " " + name + " " + attrs;
    }
}
