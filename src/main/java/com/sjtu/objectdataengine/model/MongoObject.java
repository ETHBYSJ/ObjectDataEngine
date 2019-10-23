package com.sjtu.objectdataengine.model;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Document(collection = "mongoObjects")
public class MongoObject extends MongoBase{
    private String id;              //对象id

    private String type;            //类型，event或entity
    private String template;        //模板id
    private String nodeId;          //标签id

    private HashMap<String, Date> objects; //关联的objects

    private HashMap<String, MongoAttr> attr;  //最新属性集合

    public MongoObject(String id, String type, String template, String nodeId, HashMap<String, MongoAttr> attr) {
        this.id = id;
        this.type = type;
        this.template = template;
        this.nodeId = nodeId;
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


    public HashMap<String, Date> getObjects() {
        return objects;
    }

    public void setObjects(HashMap<String, Date> objects) {
        this.objects = objects;
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
        Set<String> keySet = objects.keySet();
        for (String key : keySet) {
            Date bindTime = objects.get(key);
            if (bindTime.after(time)) {
                objects.remove(key);
            }
        }
    }
}
