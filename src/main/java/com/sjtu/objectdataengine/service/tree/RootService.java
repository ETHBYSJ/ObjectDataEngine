package com.sjtu.objectdataengine.service.tree;

import com.sjtu.objectdataengine.dao.tree.MongoRootDAO;
import com.sjtu.objectdataengine.dao.tree.RedisRootDAO;
import com.sjtu.objectdataengine.model.tree.RootMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

@Component
public class RootService {

    @Autowired
    MongoRootDAO mongoRootDAO;
    @Autowired
    RedisRootDAO redisRootDAO;

    private void create() {
        RootMessage rootMessage = new RootMessage("root", new HashMap<String, String>());
        Date now = new Date();
        rootMessage.setCreateTime(now);
        rootMessage.setUpdateTime(now);
        mongoRootDAO.create(rootMessage);
        redisRootDAO.create(rootMessage);
    }

    public RootMessage find() {
        if (mongoRootDAO.findAll(RootMessage.class).size()==0) {
            this.create();
        }
        return mongoRootDAO.findAll(RootMessage.class).get(0);
    }

    public void addNewRoot(String id, String name, Date date) {
        mongoRootDAO.addNewRoot(id, name, date);
    }
}
