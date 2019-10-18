package com.sjtu.objectdataengine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MongoObjectService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    MongoObjectDAO mongoObjectDAO;

    @Autowired
    MongoTemplateDAO mongoTemplateDAO;

    public boolean createObject(String id, String template) {
        return true;
    }
}
