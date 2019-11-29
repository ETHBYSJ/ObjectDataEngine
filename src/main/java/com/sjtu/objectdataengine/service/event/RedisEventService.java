package com.sjtu.objectdataengine.service.event;

import com.sjtu.objectdataengine.dao.event.RedisEventAttrDAO;
import com.sjtu.objectdataengine.dao.event.RedisEventDAO;
import com.sjtu.objectdataengine.dao.template.RedisTemplateDAO;
import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class RedisEventService {
    @Resource
    private RedisEventDAO redisEventDAO;
    @Resource
    private RedisEventAttrDAO redisEventAttrDAO;
    @Resource
    private RedisTemplateDAO redisTemplateDAO;

    /**
     * 创建新的事件对象
     * @param id 对象id
     * @param name 对象名
     * @param intro 对象简介
     * @param template 关联模板
     * @param attrs 属性集合
     * @return true代表创建成功，false代表创建失败
     */
    public boolean create(String id, String name, String intro, String template, HashMap<String, String> attrs) {
        if(redisEventDAO.hasKey(id + "#base")) {
            return false;
        }
        ObjectTemplate objectTemplate = redisTemplateDAO.findById(template);
        Date now = new Date();
        String baseKey = id + "#base";
        redisEventDAO.hset(baseKey, "name", name);
        redisEventDAO.hset(baseKey, "intro", intro);
        redisEventDAO.hset(baseKey, "template", template);
        HashMap<String, String> templateAttrs = objectTemplate.getAttrs();
        for(Map.Entry<String, String> attr : templateAttrs.entrySet()) {
            String attrName = attr.getKey();
            String key = id + '#' + attrName;
            String value = attrs.get(attrName) != null ? "" : attrs.get(attrName);
            //存储各属性值
            redisEventDAO.lSet(id, attrName);
            redisEventAttrDAO.hset(key, "createTime", now);
            redisEventAttrDAO.hset(key, "updateTime", now);
            redisEventAttrDAO.hset(key, "value", value);
        }
        //事件开始时间
        redisEventDAO.hset(baseKey, "startTime", now);
        redisEventDAO.hset(baseKey, "createTime", now);
        redisEventDAO.hset(baseKey, "updateTime", now);
        //状态
        redisEventDAO.hset(baseKey, "status", true);
        //索引表更新
        redisEventDAO.lSet("index", id);
        return true;
    }

    /**
     * 根据id查找事件对象
     * @param id 对象id
     * @return 事件对象
     */
    public EventObject findEventObjectById(String id) {
        if(id == null) return null;
        List<String> attrList = (List<String>) redisEventDAO.lGet(id, 0, -1);
        if(attrList == null || attrList.size() == 0) {
            return null;
        }
        String baseKey = id + "#base";
        //String attrsKey = id + "#attr";
        String objectsKey = id + "#objects";
        Object name = redisEventDAO.hget(baseKey, "name");
        if(name == null) name = "";
        Object intro = redisEventDAO.hget(baseKey, "intro");
        if(intro == null) intro = "";
        Object template = redisEventDAO.hget(baseKey, "template");
        if(template == null) template = "";
        Date startTime = null;
        Date endTime = null;
        boolean status = false;
        String stage = null;
        if(redisEventDAO.hget(baseKey, "startTime") != null) {
            startTime = (Date) redisEventDAO.hget(baseKey, "startTime");
        }
        if(redisEventDAO.hget(baseKey, "endTime") != null) {
            endTime = (Date) redisEventDAO.hget(baseKey, "endTime");
        }
        if(redisEventDAO.hget(baseKey, "status") != null) {
            status = (boolean) redisEventDAO.hget(baseKey, "status");
        }
        if(redisEventDAO.hget(baseKey, "stage") != null) {
            stage = redisEventDAO.hget(baseKey, "stage").toString();
        }
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
        eventObject.setStage(stage);
        return eventObject;
    }

    /**
     * 删除事件对象
     * @param id 对象id
     * @param template 对象关联模板
     * @return true代表删除成功，false代表删除失败
     */
    public boolean deleteEventById(String id, String template) {
        //没有此对象，删除失败
        if(!redisEventDAO.hasKey(id + "#base")) {
            return false;
        }
        //删除基本信息
        redisEventDAO.del(id + "#base");
        //删除关联对象表
        redisEventDAO.del(id + "#objects");
        List<String> attrs = (List<String>) redisEventDAO.lGet(id, 0, -1);
        //删除属性索引表
        redisEventDAO.del(id);
        //删除属性值
        for(String attr : attrs) {
            redisEventAttrDAO.del(id + '#' +attr);
        }
        //解除模板的关联
        redisTemplateDAO.lRemove(template + "#objects", 1, id);
        //索引表删除
        redisEventDAO.lRemove("index", 1, id);
        return true;
    }

    /**
     * 基本信息的更新
     * @param id 对象id
     * @param name 对象名
     * @param intro 对象简介
     * @param stage 对象状态
     * @return
     */
    public boolean updateBaseInfo(String id, String name, String intro, String stage) {
        try {
            String baseKey = id + "#base";
            if(name != null) redisEventDAO.hset(baseKey, "name", name);
            if(intro != null) redisEventDAO.hset(baseKey, "intro", intro);
            if(stage != null) redisEventDAO.hset(baseKey, "stage", stage);
            //设置更新时间
            redisEventDAO.hset(baseKey, "updateTime", new Date());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 更新事件对象属性值
     * @param id 对象id
     * @param name 属性名
     * @param value 更新后的属性值
     * @return true代表更新成功，false代表更新失败
     */
    public boolean updateAttr(String id, String name, String value) {
        try {
            String baseKey = id + "#base";
            String attrKey = id + '#' + name;
            Date now = new Date();
            redisEventDAO.hset(baseKey, "updateTime", now);
            if(!redisEventAttrDAO.hasKey(attrKey)) {
                redisEventAttrDAO.hset(attrKey, "createTime", now);
            }
            redisEventAttrDAO.hset(attrKey, "updateTime", now);
            redisEventAttrDAO.hset(attrKey, "value", value);
            return true;
        } catch(Exception e) {
            return false;
        }
    }
    public void addObject(String id, String objId) {
        redisEventDAO.opObject(id, objId, "add");
    }
    public void delObject(String id, String objId) {
        redisEventDAO.opObject(id, objId, "del");
    }
}
