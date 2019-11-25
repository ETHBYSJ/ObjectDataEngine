package com.sjtu.objectdataengine.model.tree;

import com.sjtu.objectdataengine.model.MongoBase;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Document(collection="knowledgeTree")
public class TreeNode extends MongoBase {
    private String id;          // 结点id
    private String name;        // 名称 (英文概念)
    private String intro;       // 中文概念名
    private String template;    // 对象模板

    private String parent; //父节点
    private List<String> children;

    public TreeNode(String id, String name, String intro, String template, String parent, List<String> children) {
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

    public String getIntro() {
        return intro;
    }

    public void setIntro(String intro) {
        this.intro = intro;
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

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getChildren() {
        return children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
