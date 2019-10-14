package com.sjtu.objectdataengine.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.Set;

@Document(collection="objectTemplate")
public class ObjectTemplate {
    @Id
    private String id;              //对象模板id
    private String name;            //对象模板name
    private Set<String> attr;       //属性集合
    private Date createTime;        //创建时间
    private Date updateTime;        //更新时间

    public ObjectTemplate(String id, String name, Set<String> attribute) {
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
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
