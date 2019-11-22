package com.sjtu.objectdataengine.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TreeNodeReturn extends MongoBase{
    private String id;      //结点id
    private String name;    //名称
    private String template;    //对象模板
    private String intro;
    //private Map<String, String> events; //事件
    private List<String> parents; //父节点
    private List<TreeNodeReturn> children;

    public TreeNodeReturn(String id, String name, String intro, String template, List<String> parents, List<TreeNodeReturn> children, HashMap<String, String> objects) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.template = template;
        this.parents = parents;
        this.children = children;
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

    public void setTemplate(String temp) {
        this.template = temp;
    }

    public List<String> getParents() {
        return parents;
    }

    public void setParents(List<String> parents) {
        this.parents = parents;
    }

    public List<TreeNodeReturn> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNodeReturn> children) {
        this.children = children;
    }
}
