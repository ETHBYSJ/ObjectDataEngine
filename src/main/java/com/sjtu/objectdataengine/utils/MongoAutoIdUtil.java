package com.sjtu.objectdataengine.utils;

import com.sjtu.objectdataengine.model.subscribe.MongoSequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import static org.springframework.data.mongodb.core.FindAndModifyOptions.options;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;
@Component
public class MongoAutoIdUtil {
    @Autowired
    MongoTemplate mongoTemplate;
    public Long getNextId(String collectionName) {
        MongoSequence seq = mongoTemplate.findAndModify(
                query(where("_id").is(collectionName)),
                new Update().inc("seq", 1),
                options().upsert(true).returnNew(true),
                MongoSequence.class);
        return seq.getSeq();
    }
}
