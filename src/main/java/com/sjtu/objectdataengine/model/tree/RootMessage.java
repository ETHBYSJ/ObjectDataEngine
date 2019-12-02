package com.sjtu.objectdataengine.model.tree;

import com.sjtu.objectdataengine.model.BaseModel;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;

@Document(collection="knowledgeTree")
public class RootMessage extends BaseModel {
    @Id
    private String id;
    private HashMap<String, String> roots;

    public RootMessage(String id, HashMap<String, String> roots) {
        this.id = id;
        this.roots = roots;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, String> getRoots() {
        return roots;
    }

    public void setRoots(HashMap<String, String> roots) {
        this.roots = roots;
    }
}
