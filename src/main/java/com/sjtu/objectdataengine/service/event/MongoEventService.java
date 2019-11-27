package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.utils.MongoAttr;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MongoEventService {

    @Resource
    MongoEventDAO mongoEventDAO;

    public void create(String id, String name, String intro, String template, HashMap<String, MongoAttr> attrs) {
        List<String> objects = new ArrayList<>();
        EventObject eventObject = new EventObject(id, name, intro, template, objects, attrs);
        mongoEventDAO.create(eventObject);
    }

    public void delete(String id) {
        mongoEventDAO.deleteByKey(id);
    }
}
