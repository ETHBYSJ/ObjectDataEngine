package com.sjtu.objectdataengine.model.subscribe;

import org.springframework.data.annotation.Id;

public class MongoSequence {
    @Id
    private String id;
    private Long seq;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public Long getSeq() {
        return seq;
    }
    public void setSeq(Long seq) {
        this.seq = seq;
    }

}
