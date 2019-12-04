package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoAttr;
import com.sjtu.objectdataengine.utils.MongoConditionn;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class MongoEventService {

    @Resource
    MongoEventDAO mongoEventDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    public void create(String id, String name, String intro, String template, HashMap<String, String> attrsKv, Date date) {
        List<String> objects = new ArrayList<>();
        HashMap<String, String> attrsMap = mongoTemplateDAO.findById(template, ObjectTemplate.class).getAttrs();
        HashMap<String, MongoAttr> attrs = new HashMap<>();
        for (String attr : attrsMap.keySet()) {
            String value = attrsKv.get(attr)==null ? "" : attrsKv.get(attr);
            MongoAttr mongoAttr = new MongoAttr(value);
            mongoAttr.setUpdateTime(date);
            attrs.put(attr, mongoAttr);
        }
        EventObject eventObject = new EventObject(id, name, intro, template, objects, attrs);
        eventObject.setCreateTime(date);
        eventObject.setUpdateTime(date);
        // 设置创建时间为开始时间
        eventObject.setStartTime(date);
        mongoEventDAO.create(eventObject);
    }

    public void delete(String id, String template) {
        if (mongoTemplateDAO.opObjects(template, id, "del")) {
            mongoEventDAO.deleteById(id, EventObject.class);
        }
    }

    public void end(String id) {
        MongoConditionn mongoConditionn = new MongoConditionn();
        mongoConditionn.addQuery("id", id);
        mongoConditionn.addUpdate("endTime", new Date());
        mongoConditionn.addUpdate("status", false);
        mongoEventDAO.update(mongoConditionn, EventObject.class);
    }

    public void updateBaseInfo(String id, String name, String intro, String stage, Date date) {
        boolean flag = false;
        MongoConditionn mongoConditionn = new MongoConditionn();
        mongoConditionn.addQuery("id", id);
        if (name != null) {
            mongoConditionn.addUpdate("name", name);
            flag = true;
        }
        if (intro != null) {
            mongoConditionn.addUpdate("intro", intro);
            flag = true;
        }
        if (stage != null) {
            mongoConditionn.addUpdate("stage", stage);
            flag = true;
        }
        if (flag) {
            mongoConditionn.addUpdate("updateTime", date);
            mongoEventDAO.update(mongoConditionn, EventObject.class);
        }
    }

    EventObject findEventObjectById(String id) {
        return mongoEventDAO.findById(id, EventObject.class);
    }

    public void modifyAttr(String id, String name, String value, Date date) {
        MongoConditionn mongoConditionn = new MongoConditionn();
        mongoConditionn.addQuery("id", id);
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setUpdateTime(date);
        mongoConditionn.addUpdate("attrs." + name, mongoAttr);
        mongoConditionn.addUpdate("updateTime", date);
        mongoEventDAO.update(mongoConditionn, EventObject.class);
    }

    public void addObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "add");
    }

    public void delObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "del");
    }
}
