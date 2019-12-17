package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.object.MongoAttr;
import com.sjtu.objectdataengine.utils.MongoCondition;
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

    public boolean create(String id, String name, String intro, String template, HashMap<String, String> attrsKv, Date date) {
        try {
            //id判重
            EventObject e = mongoEventDAO.findById(id, EventObject.class);
            if (e != null) return false;
            List<String> objects = new ArrayList<>();
            HashMap<String, String> attrsMap = mongoTemplateDAO.findById(template, ObjectTemplate.class).getAttrs();
            HashMap<String, MongoAttr> attrs = new HashMap<>();
            for (String attr : attrsMap.keySet()) {
                String value = attrsKv.get(attr) == null ? "" : attrsKv.get(attr);
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
            mongoTemplateDAO.opObjects(template, id, "add");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteEventById(String id, String template) {
        try {
            if (mongoTemplateDAO.opObjects(template, id, "del")) {
                mongoEventDAO.deleteById(id, EventObject.class);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void end(String id, Date date) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.whereIs("_id", id);
        mongoCondition.set("endTime", date);
        mongoCondition.set("status", false);
        mongoEventDAO.update(mongoCondition, EventObject.class);
    }

    public void updateBaseInfo(String id, String name, String intro, String stage, Date date) {
        boolean flag = false;
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.whereIs("id", id);
        if (name != null) {
            mongoCondition.set("name", name);
            flag = true;
        }
        if (intro != null) {
            mongoCondition.set("intro", intro);
            flag = true;
        }
        if (stage != null) {
            mongoCondition.set("stage", stage);
            flag = true;
        }
        if (flag) {
            mongoCondition.set("updateTime", date);
            mongoEventDAO.update(mongoCondition, EventObject.class);
        }
    }

    EventObject findEventObjectById(String id) {
        return mongoEventDAO.findById(id, EventObject.class);
    }

    public boolean modifyAttr(String id, String name, String value, Date date) {
        try {
            MongoCondition mongoCondition = new MongoCondition();
            mongoCondition.whereIs("id", id);
            MongoAttr mongoAttr = new MongoAttr(value);
            mongoAttr.setUpdateTime(date);
            mongoCondition.set("attrs." + name, mongoAttr);
            mongoCondition.set("updateTime", date);
            mongoEventDAO.update(mongoCondition, EventObject.class);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void addObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "add");
    }

    public void delObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "del");
    }
}
