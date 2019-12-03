package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.service.rabbit.RabbitMQService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class UserService {

    @Resource
    private UserDAO userDAO;
    @Resource
    private RabbitMQService rabbitMQService;

    public boolean create(String id, String name, String intro) {
        User user = new User(id, name, intro);
        return userDAO.create(user);
    }
    public boolean isUserEmpty(String id) {
        User user = userDAO.findByKey(id);
        return user.getEventSubscribe().size() == 0 && user.getObjectSubscribe().size() == 0 && user.getTemplateSubscribe().size() == 0;
    }
    public String addObjectSubscribe(String userId, String type, String objId, String name) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (objId == null || objId.equals("")) return "对象ID不能为空";

        if(type.equals("entity")) {
            userDAO.addObjectSubscribe(userId, objId, name);
        }
        else if(type.equals("template")) {
            userDAO.addTemplateSubscribe(userId, objId, name);
        }
        else {
            // event
            userDAO.addEventSubscribe(userId, objId, name);
        }
        return "增加成功";
    }
    public String delObjectSubscribe(String userId, String type, String objId, String name) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if (!(type.equals("entity") || type.equals("template") || type.equals("event"))) return "类型错误";
        if (objId == null || objId.equals("")) return "对象ID不能为空";

        if(type.equals("entity")) {
            userDAO.delObjectSubscribe(userId, objId, name);
            if(isUserEmpty(userId)) {
                userDAO.deleteByKey(userId);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
        }
        else if(type.equals("template")) {
            userDAO.delTemplateSubscribe(userId, objId, name);
            if(isUserEmpty(userId)) {
                userDAO.deleteByKey(userId);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
        }
        else {
            // event
            userDAO.delEventSubscribe(userId, objId, name);
            if(isUserEmpty(userId)) {
                userDAO.deleteByKey(userId);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
        }
        return "删除成功";
    }
}
