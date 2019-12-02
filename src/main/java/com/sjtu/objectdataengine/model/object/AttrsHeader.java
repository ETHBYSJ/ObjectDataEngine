package com.sjtu.objectdataengine.model.object;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "mongoHeader")
public class AttrsHeader extends BaseModel {
    @Id
    private String id;
    private String objId;
    private String attrName;
    private int size; //链条长度

    public AttrsHeader(String id, String objId, String attrName, int size) {
        this.id = id;
        this.objId = objId;
        this.attrName = attrName;
        this.size = size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void addSize() {
        this.size += 1;
    }
}
