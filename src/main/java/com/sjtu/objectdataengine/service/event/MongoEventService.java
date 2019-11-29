package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoAttr;
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

    public void create(String id, String name, String intro, String template, HashMap<String, String> attrsKv, Date date) {
        List<String> objects = new ArrayList<>();
        HashMap<String, String> attrsMap = mongoTemplateDAO.findByKey(template).getAttrs();
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
            mongoEventDAO.deleteByKey(id);
        }
    }

    public void end(String id) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", id);
        mongoCondition.addUpdate("endTime", new Date());
        mongoCondition.addUpdate("status", false);
        mongoEventDAO.update(mongoCondition);
    }

    public void updateBaseInfo(String id, String name, String intro, String stage, Date date) {
        boolean flag = false;
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", id);
        if (name != null) {
            mongoCondition.addUpdate("name", name);
            flag = true;
        }
        if (intro != null) {
            mongoCondition.addUpdate("intro", intro);
            flag = true;
        }
        if (stage != null) {
            mongoCondition.addUpdate("stage", stage);
            flag = true;
        }
        if (flag) {
            mongoCondition.addUpdate("updateTime", date);
            mongoEventDAO.update(mongoCondition);
        }
    }

    EventObject findEventObjectById(String id) {
        return mongoEventDAO.findByKey(id);
    }

    public void modifyAttr(String id, String name, String value, Date date) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", id);
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setUpdateTime(date);
        mongoCondition.addUpdate("attrs." + name, mongoAttr);
        mongoCondition.addUpdate("updateTime", date);
        mongoEventDAO.update(mongoCondition);
    }

    public void addObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "add");
    }

    public void delObject(String id, String objId) {
        mongoEventDAO.opObjects(id, objId, "del");
    }
}
