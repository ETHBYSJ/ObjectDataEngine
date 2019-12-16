package com.sjtu.objectdataengine.model.subscribe;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 一个对象对应一个实体对象或模板
 * id = objId + type
 */

@Document(collection = "subscribeMessage")
public class BaseSubscribeMessage extends BaseModel {
    @Id
    protected String id;          // object Id + type
    protected String type;        // type: event or entity or template

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

}
