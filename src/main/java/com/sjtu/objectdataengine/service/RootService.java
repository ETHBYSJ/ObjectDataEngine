package com.sjtu.objectdataengine.service;

import com.sjtu.objectdataengine.dao.MongoRootDAO;
import com.sjtu.objectdataengine.model.RootMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class RootService {

    @Autowired
    MongoRootDAO mongoRootDAO;

    private void create() {
        RootMessage rootMessage = new RootMessage("root", new HashMap<String, String>());
        mongoRootDAO.create(rootMessage);
    }

    public RootMessage find() {
        //System.out.println("kaiqi" + mongoRootDAO.findAll());
        if (mongoRootDAO.findAll().size()==0) {
            this.create();
        }
        return mongoRootDAO.findAll().get(0);
    }

    public void addNewRoot(String id, String name) {
        mongoRootDAO.addNewRoot(id, name);
    }
}
