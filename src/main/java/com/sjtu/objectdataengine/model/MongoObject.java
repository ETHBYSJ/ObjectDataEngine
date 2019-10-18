package com.sjtu.objectdataengine.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MongoObject extends MongoBase{
    private String id;              //对象id

    private String type;            //类型，event或entity
    private String template;        //模板id
    private String nodeId;          //标签id

    private Date endTime;           //结束时间，只有活动有意义
    private HashMap<String, Date> objects; //关联的objects

    private HashMap<String, List<MongoAttr>> attr;  //属性集合

    MongoObject(String id, String type, String template, String nodeId, Date endTime, HashMap<String, List<MongoAttr>> attr) {
        this.id = id;
        this.type = type;
        this.template = template;
        this.nodeId = nodeId;
        this.endTime = endTime;
        this.attr = attr;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public HashMap<String, Date> getObjects() {
        return objects;
    }

    public void setObjects(HashMap<String, Date> objects) {
        this.objects = objects;
    }

    public HashMap<String, List<MongoAttr>> getAttr() {
        return attr;
    }

    public void setAttr(HashMap<String, List<MongoAttr>> attr) {
        this.attr = attr;
    }

    public void addValue(String name, String value, Date time) {
        MongoAttr mongoAttr = new MongoAttr(value, time);
        List<MongoAttr> mongoAttrs = this.attr.get(name);
        mongoAttrs.add(mongoAttr);
        this.attr.put(name, mongoAttrs);
    }
}
