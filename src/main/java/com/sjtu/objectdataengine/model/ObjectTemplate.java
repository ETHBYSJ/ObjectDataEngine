package com.sjtu.objectdataengine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection="objectTemplate")
public class ObjectTemplate {
    @Id
    private String id;              //对象模板id
    private String name;            //对象模板name
    private Set<String> attr;      //属性集合

    ObjectTemplate(String id, String name, Set<String> attribute) {
        this.id = id;
        this.name = name;
        this.attr = attribute;
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

    public Set<String> getAttr() {
        return attr;
    }

    public void setAttr(Set<String> attribute) {
        this.attr = attribute;
    }

    public boolean addAttr(String name) {
        return this.attr.add(name);
    }

    public boolean delAttr(String name) {
        return this.attr.remove(name);
    }

    public boolean replaceAttr(String oldName, String newName) {
        if (this.attr.remove(oldName)) {
            return this.attr.add(newName);
        } else {
            return false;
        }
    }
}
