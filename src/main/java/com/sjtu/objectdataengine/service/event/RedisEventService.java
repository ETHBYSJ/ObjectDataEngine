package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.RedisEventAttrDAO;
import com.sjtu.objectdataengine.dao.event.RedisEventDAO;
import com.sjtu.objectdataengine.model.object.EventObject;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class RedisEventService {
    @Autowired
    private RedisEventDAO redisEventDAO;
    @Autowired
    private RedisEventAttrDAO redisEventAttrDAO;

    public EventObject findEventObjectById(String id) {
        if(id == null) return null;
        List<String> attrList = (List<String>) redisEventDAO.lGet(id, 0, -1);
        if(attrList == null || attrList.size() == 0) {
            return null;
        }
        String baseKey = id + "#base";
        String attrsKey = id + "#attr";
        String objectsKey = id + "#objects";
        Object name = redisEventDAO.hget(baseKey, "name");
        if(name == null) name = "";
        Object intro = redisEventDAO.hget(baseKey, "intro");
        if(intro == null) intro = "";
        Object template = redisEventDAO.hget(baseKey, "template");
        if(template == null) template = "";
        Date startTime = (Date) redisEventDAO.hget(baseKey, "startTime");
        Date endTime = (Date) redisEventDAO.hget(baseKey, "endTime");
        boolean status = (boolean) redisEventDAO.hget(baseKey, "status");
        List<String> objects = (List<String>) redisEventDAO.lGet(objectsKey, 0, -1);
        HashMap<String, MongoAttr> attrMap = new HashMap<>();
        for(String attr : attrList) {
            MongoAttr mongoAttr = redisEventAttrDAO.findAttr(id, attr);
            attrMap.put(attr, mongoAttr);
        }
        EventObject eventObject = new EventObject(id, name.toString(), intro.toString(), template.toString(), objects, attrMap);
        eventObject.setStatus(status);
        eventObject.setStartTime(startTime);
        eventObject.setEndTime(endTime);
        return eventObject;
    }
}
