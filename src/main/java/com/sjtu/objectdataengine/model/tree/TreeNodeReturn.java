package com.sjtu.objectdataengine.model.tree;

import com.sjtu.objectdataengine.model.BaseModel;

import java.util.HashMap;
import java.util.List;


public class TreeNodeReturn extends BaseModel {
    private String id;      //结点id
    private String name;    //名称
    private String template;    //对象模板
    private String intro;
    //private Map<String, String> events; //事件
    private String parent;  //父节点
    private List<TreeNodeReturn> children;

    public TreeNodeReturn(String id, String name, String intro, String template, String parent, List<TreeNodeReturn> children, HashMap<String, String> objects) {
        this.id = id;
        this.name = name;
        this.intro = intro;
        this.template = template;
        this.parent = parent;
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<TreeNodeReturn> getChildren() {
        return children;
    }

    public void setChildren(List<TreeNodeReturn> children) {
        this.children = children;
    }
}
