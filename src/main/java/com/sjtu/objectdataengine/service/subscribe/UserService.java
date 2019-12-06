package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.subscribe.SubscribeDAO;
import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.rabbitMQ.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.rabbit.RabbitMQService;
import com.sjtu.objectdataengine.utils.MongoAutoIdUtil;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@Component
public class UserService {

    @Resource
    private UserDAO userDAO;
    @Resource
    private SubscribeDAO subscribeDAO;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private MongoAutoIdUtil mongoAutoIdUtil;
    @Resource
    private SubscribeSender subscribeSender;

    /**
     * 用户注册，注册时分配唯一id
     * @param name 用户名
     * @param intro 简介
     * @return 分配给用户的唯一id
     */
    public String register(String name, String intro) {
        String id = mongoAutoIdUtil.getNextId("seq_user").toString();
        this.create(id, name, intro);
        rabbitMQService.addQueue(id, id);
        //subscribeSender.send
        return id;
    }

    /**
     * 注销用户，删除对应队列
     * @param id 分配给用户的唯一id
     * @return true代表注销成功，false代表注销失败
     */
    public boolean unregister(String id) {
        User user = userDAO.findById(id, User.class);
        if(user == null) {
            return false;
        }
        List<String> eventSubscribe = user.getEventSubscribe();
        List<String> objectSubscribe = user.getObjectSubscribe();
        List<String> templateSubscribe = user.getTemplateSubscribe();
        // 从用户表中删除
        userDAO.deleteById(id, User.class);
        // 从订阅表中删除该用户
        for(String event : eventSubscribe) {
            String[] eventSplit = event.split(":");
            if(eventSplit.length == 1) {
                subscribeDAO.delObjectSubscriber(eventSplit[0], "event", id);
            }
            else {
                // eventSplit.length == 2
                subscribeDAO.delAttrSubscriber(eventSplit[0], "event", eventSplit[1], id);
            }
        }
        for(String object : objectSubscribe) {
            String[] objectSplit = object.split(":");
            if(objectSplit.length == 1) {
                subscribeDAO.delObjectSubscriber(objectSplit[0], "object", id);
            }
            else {
                // objectSplit.length == 2
                subscribeDAO.delAttrSubscriber(objectSplit[0], "object", objectSplit[1], id);
            }
        }
        for(String template : templateSubscribe) {
            String[] templateSplit = template.split(":");
            if(templateSplit.length == 1) {
                subscribeDAO.delObjectSubscriber(templateSplit[0], "template", id);
            }
            else {
                // templateSplit.length == 2
                subscribeDAO.delAttrSubscriber(templateSplit[0], "template", templateSplit[1], id);
            }
        }
        // 删除队列
        rabbitMQService.delQueue(id);
        return true;
    }
    private boolean create(String id, String name, String intro) {
        User user = new User(id, name, intro);
        return userDAO.create(user);
    }

    public boolean isUserEmpty(String id) {
        User user = userDAO.findById(id, User.class);
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
            /*
            if(isUserEmpty(userId)) {
                userDAO.deleteById(userId, User.class);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
            */
        }
        else if(type.equals("template")) {
            userDAO.delTemplateSubscribe(userId, objId, name);
            /*
            if(isUserEmpty(userId)) {
                userDAO.deleteById(userId, User.class);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
            */
        }
        else {
            // event
            userDAO.delEventSubscribe(userId, objId, name);
            /*
            if(isUserEmpty(userId)) {
                userDAO.deleteById(userId, User.class);
                // 删除队列(队列名是否是userId?)
                rabbitMQService.delQueue(userId);
            }
            */
        }
        return "删除成功";
    }
}
