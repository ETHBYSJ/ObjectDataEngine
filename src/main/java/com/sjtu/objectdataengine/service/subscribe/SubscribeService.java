package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.event.MongoEventDAO;
import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.subscribe.SubscribeDAO;
import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.subscribe.SubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.User;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class SubscribeService {

    @Resource
    SubscribeDAO subscribeDAO;

    @Resource
    MongoEventDAO mongoEventDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    @Resource
    MongoObjectDAO mongoObjectDAO;

    @Resource
    UserDAO userDAO;

    /**
     * 删除订阅表
     * @param id 表id
     * @return true or false
     */
    public boolean deleteByIdAndType(String id, String type) {
        SubscribeMessage subscribeMessage = this.findByIdAndType(id, type);
        return subscribeDAO.deleteByKey(id + type);
    }
    /**
     * 对内自动创建
     * @param objId 对象id
     * @param type 对象类型
     * @return true or false
     */
    public boolean create(String objId, String type) {
        HashMap<String, List<String>> attrsMap = new HashMap<>();
        Date now = new Date();
        // 实体对象订阅
        switch (type) {
            case "entity": {
                Set<String> attrs = mongoObjectDAO.findByKey(objId).getAttr().keySet();
                for (String attr : attrs) {
                    attrsMap.put(attr, new ArrayList<>());
                }
                break;
            }
            // 事件对象订阅
            case "event": {
                Set<String> attrs = mongoEventDAO.findByKey(objId).getAttrs().keySet();
                for (String attr : attrs) {
                    attrsMap.put(attr, new ArrayList<>());
                }
                break;
            }
            // 根据模板订阅
            case "template": {
                Set<String> attrs = mongoTemplateDAO.findByKey(objId).getAttrs().keySet();
                for (String attr : attrs) {
                    attrsMap.put(attr, new ArrayList<>());
                }
                break;
            }
            default:
                return false;
        }
        SubscribeMessage subscribeMessage = new SubscribeMessage(objId, type, attrsMap);
        // 设置时间
        subscribeMessage.setCreateTime(now);
        subscribeMessage.setUpdateTime(now);
        return subscribeDAO.create(subscribeMessage);
    }

    /**
     * 增加属性订阅者
     * @param objId 对象id
     * @param type 类型
     * @param name 属性名称
     * @param user 订阅者id
     * @return 结果说明
     */
    public String addAttrSubscriber(String objId, String type, String name, String user) {
        // 检测空
        if (objId == null || objId.equals("")) return "对象ID不能为空";
        if (name == null || name.equals("")) return "属性name不能为空";
        if (type == null || type.equals("")) return "类型不能为空";
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (user == null || user.equals("")) return "用户ID不能为空";
        // 检测用户是否存在
        if(!userDAO.hasUser(user)) {
            return "用户不存在";
        }
        if (subscribeDAO.addAttrSubscriber(objId, type, name, user)) {
            if(type.equals("entity")) {
                userDAO.addObjectSubscribe(user, objId, name);
            }
            else if(type.equals("template")) {
                userDAO.addTemplateSubscribe(user, objId, name);
            }
            else {
                // event
                userDAO.addEventSubscribe(user, objId, name);
            }
            return "增加成功";
        }
        return "增加失败";
    }

    /**
     * 删除属性订阅者
     * @param objId 对象id
     * @param type 类型
     * @param name 属性名称
     * @param user 订阅者id
     * @return 结果说明
     */
    public String delAttrSubscriber(String objId, String type, String name, String user) {
        // 检测空
        if (objId == null || objId.equals("")) return "对象ID不能为空";
        if (name == null || name.equals("")) return "属性name不能为空";
        if (type == null || type.equals("")) return "类型不能为空";
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (user == null || user.equals("")) return "用户ID不能为空";

        if (subscribeDAO.delAttrSubscriber(objId, type, name, user)) {
            if(type.equals("entity")) {
                userDAO.delObjectSubscribe(user, objId, name);
            }
            else if(type.equals("template")) {
                userDAO.delTemplateSubscribe(user, objId, name);
            }
            else {
                // event
                userDAO.delEventSubscribe(user, objId, name);
            }
            return "删除成功";
        }
        return "删除失败";
    }

    /**
     * 增加对象订阅者
     * @param objId 对象id
     * @param user 订阅者id
     * @return 结果说明
     */
    public String addObjectSubscriber(String objId, String type, String user) {
        // 检测空
        if (objId == null || objId.equals("")) return "对象ID不能为空";
        if (type == null || type.equals("")) return "类型不能为空";
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (user == null || user.equals("")) return "用户ID不能为空";
        // 检测用户是否存在
        if(!userDAO.hasUser(user)) {
            return "用户不存在";
        }
        if (subscribeDAO.addObjectSubscriber(objId, type, user)) {
            if(type.equals("entity")) {
                userDAO.addObjectSubscribe(user, objId);
            }
            else if(type.equals("template")) {
                userDAO.addTemplateSubscribe(user, objId);
            }
            else {
                // event
                userDAO.addEventSubscribe(user, objId);
            }
            return "增加成功";
        }
        return "增加失败";
    }

    /**
     * 删除对象订阅者
     * @param objId 对象id
     * @param user 订阅者id
     * @return 结果说明
     */
    public String delObjectSubscriber(String objId, String type, String user) {
        // 检测空
        if (objId == null || objId.equals("")) return "对象ID不能为空";
        if (type == null || type.equals("")) return "类型不能为空";
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (user == null || user.equals("")) return "用户ID不能为空";
        // 检测用户是否存在
        if(!userDAO.hasUser(user)) {
            return "用户不存在";
        }
        if (subscribeDAO.delObjectSubscriber(objId, type, user)) {
            if(type.equals("entity")) {
                userDAO.delObjectSubscribe(user, objId);
            }
            else if(type.equals("template")) {
                userDAO.delTemplateSubscribe(user, objId);
            }
            else {
                // event
                userDAO.delEventSubscribe(user, objId);
            }
            return "删除成功";
        }
        return "删除失败";
    }

    /**
     *
     */
    public SubscribeMessage findByIdAndType(String objId, String type) {
        return subscribeDAO.findByKey(objId + type);
    }
}
