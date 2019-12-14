package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.subscribe.SubscribeDAO;
import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.subscribe.EntitySubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.TemplateSubscribeMessage;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class SubscribeService {
    // 订阅总服务，联系各订阅分服务和用户服务
    @Resource
    SubscribeDAO subscribeDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    @Resource
    MongoObjectDAO mongoObjectDAO;

    @Resource
    UserDAO userDAO;

    @Resource
    UserService userService;

    @Resource
    TemplateSubscribeService templateSubscribeService;

    @Resource
    EntitySubscribeService entitySubscribeService;

    @Resource
    APIObjectService objectService;

    @Resource
    APITemplateService templateService;

    /**
     * 删除订阅表
     * @param id 表id
     * @return true or false
     */

    public String deleteByIdAndType(String id, String type) {
        if(type.equals("template")) {
            TemplateSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(id);
            if(templateSubscribeMessage == null) return "模板订阅表不存在";
            // 从用户表中删除
            List<String> templateSubscriber = templateSubscribeMessage.getObjectSubscriber();
            for(String user : templateSubscriber) {
                userService.delTemplateSubscribe(user, id);
            }
            // 从订阅表中删除
            if(templateSubscribeService.deleteById(id)) {
                return "删除成功";
            }
            return "删除失败";
        }
        else if(type.equals("entity")) {
            EntitySubscribeMessage entitySubscribeMessage = entitySubscribeService.findById(id);
            if(entitySubscribeMessage == null) return "实体对象订阅表不存在";
            // 从用户表中删除
            List<String> objectSubscriber = entitySubscribeMessage.getObjectSubscriber();
            for(String user : objectSubscriber) {
                userService.delObjectSubscribe(user, id, null);
            }
            HashMap<String, List<String>> attrsSubscriber = entitySubscribeMessage.getAttrsSubscriber();
            for(Map.Entry<String, List<String>> entry : attrsSubscriber.entrySet()) {
                String name = entry.getKey();
                List<String> attrSubscriber = entry.getValue();
                for(String user : attrSubscriber) {
                    userService.delObjectSubscribe(user, id, name);
                }
            }
            // 从订阅表中删除
            if(entitySubscribeService.deleteById(id)) {
                return "删除成功";
            }
            return "删除失败";
        }
        else return "类型错误";
    }

    /**
     * 创建订阅表
     * @param type 对象类型
     * @return true or false
     */
    public boolean create(String id, String type) {
        Date date = new Date();
        if(type.equals("entity")) {
            return entitySubscribeService.create(id, date);
        }
        else if(type.equals("template")) {
            return templateSubscribeService.create(id, date);
        }
        else {
            return false;
        }
    }

    /**
     * 判断是否有对象对应的订阅消息
     * @param id 对象id
     * @param type 对象类型
     * @return true or false
     */
    public boolean hasSubscribeMessage(String id, String type) {
        if(type.equals("entity")) {
            return entitySubscribeService.findById(id) != null;
        }
        else if(type.equals("template")) {
            return templateSubscribeService.findById(id) != null;
        }
        else {
            return false;
        }
    }
    /**
     * 增加对象订阅者
     * @param user 订阅者id
     * @return 结果说明
     */

    public String addEntitySubscriber(String id, String user, String name) {
        // 检测空
        if(id == null || id.equals("")) return "对象id不能为空";
        if (user == null || user.equals("")) return "用户id不能为空";
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return "用户不存在";
        }
        // 1.检查对象是否存在
        if(objectService.findObjectById(id) == null) {
            return "实体对象不存在";
        }
        // 2.订阅表如果不存在，创建订阅表
        if(entitySubscribeService.findById(id) == null) {
            entitySubscribeService.create(id, date);
        }
        // 双向操作
        if(name == null) {
            if(entitySubscribeService.addEntitySubscriber(id, user)) {
                userService.addObjectSubscribe(user, id, null);
                return "增加成功";
            }
            return "增加失败";
        }
        else {
            if(entitySubscribeService.addEntityAttrSubscriber(id, name, user)) {
                userService.addObjectSubscribe(user, id, name);
                return "增加成功";
            }
            return "增加失败";
        }
    }

    /**
     * 删除对象订阅者
     * @param user 订阅者id
     * @return 结果说明
     */
    public String delEntitySubscriber(String id, String user, String name) {
        // 检测空
        if(id == null || id.equals("")) return "对象id不能为空";
        if (user == null || user.equals("")) return "用户id不能为空";
        // 检测用户
        if(!userService.hasUser(user)) {
            return "用户不存在";
        }
        // 双向操作
        if(name == null) {
            if(entitySubscribeService.delEntitySubscriber(id, user)) {
                userService.delObjectSubscribe(user, id, null);
                return "删除成功";
            }
            return "删除失败";
        }
        else {
            if(entitySubscribeService.delEntityAttrSubscriber(id, name, user)) {
                userService.delObjectSubscribe(user, id, name);
                return "删除成功";
            }
            return "删除失败";
        }
    }

    /**
     *
     * @param id 模板id
     * @param user 用户id
     * @param list 绑定事件列表或者实体对象列表
     * @return 说明消息
     */
    public String addTemplateSubscriber(String id, String user, List<String> list) {
        // 检测空
        if(id == null || id.equals("")) return "对象id不能为空";
        if (user == null || user.equals("")) return "用户id不能为空";
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return "用户不存在";
        }
        // 1.检查对象是否存在
        if(templateService.get(id) == null) {
            return "实体对象不存在";
        }
        // 2.订阅表如果不存在，创建订阅表
        if(templateSubscribeService.findById(id) == null) {
            templateSubscribeService.create(id, date);
        }
        // 双向操作
        if(templateSubscribeService.addTemplateSubscriber(id, user)) {
            userService.addTemplateSubscribe(user, id, list);
            return "增加成功";
        }
        return "增加失败";
    }

    /**
     *
     * @param id 模板id
     * @param user 用户id
     * @return 说明消息
     */
    public String delTemplateSubscriber(String id, String user) {
        // 检测空
        if(id == null || id.equals("")) return "对象id不能为空";
        if (user == null || user.equals("")) return "用户id不能为空";
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return "用户不存在";
        }
        // 双向操作
        if(templateSubscribeService.delTemplateSubscriber(id, user)) {
            userService.delTemplateSubscribe(user, id);
            return "删除成功";
        }
        return "删除失败";
    }
}
