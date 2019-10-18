package com.sjtu.objectdataengine.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Document(collection="knowledgeTree")
public class KnowledgeTreeNode extends MongoBase{
    private String id;      //结点id
    private String name;    //名称
    private String template;    //对象模板


    //private Map<String, String> events; //事件
    private List<String> parents; //父节点
    private List<String> children;
    private List<String> objects;

    public KnowledgeTreeNode(String id, String name, String template, List<String> parents, List<String> children, List<String> objects) {
        this.id = id;
        this.name = name;
        this.template = template;
        this.parents = parents;
        this.children = children;
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

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String temp) {
        this.template = temp;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<String> getObjects() {
        return objects;
    }

    public void setObjects(List<String> objects) {
        this.objects = objects;
    }
}
