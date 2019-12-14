package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.subscribe.EntitySubscribeDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.model.subscribe.EntitySubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.TemplateSubscribeMessage;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class EntitySubscribeService {

    @Resource
    EntitySubscribeDAO entitySubscribeDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    @Resource
    MongoObjectDAO mongoObjectDAO;

    /**
     * 根据实体id删除订阅表
     * @param id 模板id
     * @return true or false
     */
    public boolean deleteById(String id) {
        // 检查统一放在上层
        return entitySubscribeDAO.deleteById(id + "entity", EntitySubscribeMessage.class);
    }

    /**
     * 根据id创建订阅表
     * @param id 模板id
     * @return true or false
     */
    /*
    public boolean create(String id, Date date) {
        EntitySubscribeMessage entitySubscribeMessage = new EntitySubscribeMessage(id, "entity");
        return entitySubscribeDAO.create(entitySubscribeMessage);
    }
    */
    public boolean create(String id, Date date) {
        if(this.findById(id) != null) return false;
        EntitySubscribeMessage entitySubscribeMessage = new EntitySubscribeMessage(id, "entity");
        HashMap<String, List<String>> attrsMap = new HashMap<>();
        CommonObject obj = mongoObjectDAO.findById(id, CommonObject.class);
        if(obj == null) return false;
        Set<String> attrs = obj.getAttrs().keySet();
        for(String attr : attrs) {
            attrsMap.put(attr, new ArrayList<>());
        }
        entitySubscribeMessage.setAttrsSubscriber(attrsMap);
        entitySubscribeMessage.setCreateTime(date);
        entitySubscribeMessage.setUpdateTime(date);
        return entitySubscribeDAO.create(entitySubscribeMessage);
    }
    /**
     * 增加订阅者
     * @param id 实体对象id
     * @param userId 用户id
     * @return true or false
     */
    public boolean addEntitySubscriber(String id, String userId) {
        return entitySubscribeDAO.addObjectSubscriber(id, "entity", userId);
    }

    /**
     * 删除订阅者
     * @param id 实体对象id
     * @param userId
     * @return true or false
     */
    public boolean delEntitySubscriber(String id, String userId) {
        return entitySubscribeDAO.delObjectSubscriber(id, "entity", userId);
    }

    /**
     * 根据id查找订阅消息
     * @param id 实体对象id
     * @return 订阅消息
     */
    public EntitySubscribeMessage findById(String id) {
        return entitySubscribeDAO.findById(id + "entity", EntitySubscribeMessage.class);
    }

    /**
     * 删除实体对象某属性的订阅者
     * @param id 实体对象id
     * @param name 属性名
     * @param userId 用户id
     * @return true or false
     */
    public boolean delEntityAttrSubscriber(String id, String name, String userId) {
        return entitySubscribeDAO.delAttrSubscriber(id, "entity", name, userId);
    }

    /**
     * 增加实体对象某属性的订阅者
     * @param id 实体对象id
     * @param name 属性名
     * @param userId 用户id
     * @return true or false
     */
    public boolean addEntityAttrSubscriber(String id, String name, String userId) {
        return entitySubscribeDAO.addAttrSubscriber(id, "entity", name, userId);
    }
}
