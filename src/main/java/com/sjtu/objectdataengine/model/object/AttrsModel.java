package com.sjtu.objectdataengine.model.object;

import com.sjtu.objectdataengine.config.Constants;
import com.sjtu.objectdataengine.model.BaseModel;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * 每条属性都是一个document
 */

@Document(collection = "mongoAttrs")
public class AttrsModel extends BaseModel {
    @Id
    private String id;

    private List<MongoAttr> attrs;

    private int index; //位置
    private int size; //已有长度

    public AttrsModel(String id, List<MongoAttr> attrs, int index) {
        this.id = id;
        this.attrs = attrs;
        this.size = attrs.size();
        this.index = index;
    }

    public int getSize() {
        return size;
    }

    public void addSize() {
        this.size += 1;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public List<MongoAttr> getAttrs() {
        return attrs;
    }

    public void setAttrs(List<MongoAttr> attrs) {
        this.attrs = attrs;
    }

    public void addAttr(MongoAttr mongoAttr) {
        this.attrs.add(mongoAttr);
    }

    public boolean isFull() {
        return this.size >= Constants.BLOCK_LENGTH;
    }

    public boolean isNearlyFull() {
        return this.size >= Constants.BLOCK_LENGTH - 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "AttrsModel: { id: " + id + ", size: " + size + " }";
    }
}
