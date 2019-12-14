package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.subscribe.SubscribeDAO;
import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.rabbit.RabbitMQService;
import com.sjtu.objectdataengine.utils.MongoAutoIdUtil;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.setQuery(new Query().addCriteria(Criteria.where("name").is(name)));
        if(userDAO.findByArgs(mongoCondition, User.class).size() != 0) {
            return "用户名重复";
        }
        String id = mongoAutoIdUtil.getNextId("seq_user").toString();
        this.create(id, name, intro);
        rabbitMQService.addQueue(name, id);
        return id;
    }

    /**
     * 注销用户，删除对应队列
     * @param id 分配给用户的唯一id
     * @return true代表注销成功，false代表注销失败
     */
    /*
    public boolean unregister(String id) {
        User user = userDAO.findById(id, User.class);
        Map<String, String> map = new HashMap<>();
        if(user == null) {
            //map.put("status", "FAIL");
            subscribeSender.send(JSON.toJSONString(map), id);
            return false;
        }
        List<String> objectSubscribe = user.getObjectSubscribe();
        List<String> templateSubscribe = user.getTemplateSubscribe();
        // 从用户表中删除
        userDAO.deleteById(id, User.class);
        // 从订阅表中删除该用户
        for(String object : objectSubscribe) {
            String[] objectSplit = object.split(":");
            if(objectSplit.length == 1) {
                subscribeDAO.delObjectSubscriber(objectSplit[0], "entity", id);
            }
            else {
                // objectSplit.length == 2
                subscribeDAO.delAttrSubscriber(objectSplit[0], "entity", objectSplit[1], id);
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

     */
    public boolean create(String id, String name, String intro) {
        User user = new User(id, name, intro);
        return userDAO.create(user);
    }

    public boolean hasUser(String user) {
        return userDAO.findById(user, User.class) != null;
    }

    /**
     * 增加实体对象订阅
     * @param userId 用户id
     * @param id 实体对象id
     * @param name 属性名，为null表示订阅整个对象
     * @return 说明信息
     */
    public String addObjectSubscribe(String userId, String id, String name) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.addObjectSubscribe(userId, id, name);
        return "增加成功";
    }

    /**
     * 删除实体对象订阅
     * @param userId 用户id
     * @param id 实体对象id
     * @param name 属性名，可为null
     * @return 说明信息
     */
    public String delObjectSubscribe(String userId, String id, String name) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.delObjectSubscribe(userId, id, name);
        return "删除成功";
    }

    public String addTemplateSubscribe(String userId, String id, List<String> list) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        // 待：检查关联列表的有效性
        userDAO.addTemplateSubscribe(userId, id, list);
        return "增加成功";
    }

    public String delTemplateSubscribe(String userId, String id) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.delTemplateSubscribe(userId, id);
        return "删除成功";
    }
}
