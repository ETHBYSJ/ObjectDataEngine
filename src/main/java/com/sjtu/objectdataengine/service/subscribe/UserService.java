package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.model.subscribe.User;
import com.sjtu.objectdataengine.rabbitMQ.outside.sender.SubscribeSender;
import com.sjtu.objectdataengine.service.rabbit.RabbitMQService;
import com.sjtu.objectdataengine.utils.MongoAutoIdUtil;
import com.sjtu.objectdataengine.utils.MongoCondition;
import com.sjtu.objectdataengine.utils.Result.Result;
import com.sjtu.objectdataengine.utils.Result.ResultCodeEnum;
import com.sjtu.objectdataengine.utils.Result.ResultData;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UserService {

    @Resource
    private UserDAO userDAO;
    @Resource
    private RabbitMQService rabbitMQService;
    @Resource
    private MongoAutoIdUtil mongoAutoIdUtil;
    @Resource
    private EntitySubscribeService entitySubscribeService;
    @Resource
    private TemplateSubscribeService templateSubscribeService;

    /**
     * 查找全部用户
     * @return 用户列表
     */
    public List<User> findAll() {
        return userDAO.findAll(User.class);
    }
    /**
     * 用户注册，注册时分配唯一id
     * @param name 用户名
     * @param intro 简介
     * @return 分配给用户的唯一id
     */
    public ResultInterface register(String name, String intro) {
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.setQuery(new Query().addCriteria(Criteria.where("name").is(name)));
        if(userDAO.findByArgs(mongoCondition, User.class).size() != 0) {
            return Result.build(ResultCodeEnum.USER_REGISTER_DUPLICATE_USERNAME);
        }
        String id = mongoAutoIdUtil.getNextId("seq_user").toString();
        this.create(id, name, intro);
        rabbitMQService.addQueue(name, id);
        return ResultData.build(ResultCodeEnum.USER_REGISTER_SUCCESS, id);
    }

    /**
     * 注销用户，删除对应队列
     * @param id 分配给用户的唯一id
     * @return true代表注销成功，false代表注销失败
     */
    public ResultInterface unregister(String id) {
        User user = userDAO.findById(id, User.class);
        Map<String, String> map = new HashMap<>();
        if(user == null) {
            return Result.build(ResultCodeEnum.USER_UNREGISTER_FAIL);
        }
        List<String> objectSubscribe = user.getObjectSubscribe();
        HashMap<String, List<String>> attrsSubscribeMap = user.getAttrsSubscribe();
        HashMap<String, List<String>> templateSubscribe = user.getTemplateSubscribe();
        // 从用户表中删除
        userDAO.deleteById(id, User.class);
        // 从订阅表中删除该用户
        for(String object : objectSubscribe) {
            entitySubscribeService.delEntitySubscriber(object, id);
        }
        for(Map.Entry<String, List<String>> entry : attrsSubscribeMap.entrySet()) {
            List<String> attrList = entry.getValue();
            String object = entry.getKey();
            for(String attr : attrList) {
                entitySubscribeService.delEntityAttrSubscriber(object, attr, id);
            }
        }
        for(Map.Entry<String, List<String>> entry : templateSubscribe.entrySet()) {
            String template = entry.getKey();
            templateSubscribeService.delTemplateSubscriber(template, id);
        }
        // 删除队列
        rabbitMQService.delQueue(id);
        return Result.build(ResultCodeEnum.USER_UNREGISTER_SUCCESS);
    }

    /**
     *
     * @param id id 用户id
     * @param name 用户名
     * @param intro 简介
     * @return true or false
     */
    public boolean create(String id, String name, String intro) {
        User user = new User(id, name, intro);
        return userDAO.create(user);
    }

    public boolean hasUser(String user) {
        return userDAO.findById(user, User.class) != null;
    }
    public User findUserById(String user) {
        return userDAO.findById(user, User.class);
    }
    public String addAttrSubscribe(String userId, String id, String attr) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.addAttrSubscribe(userId, id, attr);
        return "增加成功";
    }
    public String delAttrSubscribe(String userId, String id, String attr) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.delAttrSubscribe(userId, id, attr);
        return "删除成功";
    }
    /**
     * 增加属性订阅
     * @param userId 用户id
     * @param id 对象id
     * @param attrs 属性列表
     * @return 说明信息
     */
    public String addAttrSubscribe(String userId, String id, List<String> attrs) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        // attrs必不为空
        userDAO.addAttrSubscribe(userId, id, attrs);
        return "增加成功";
    }
    /**
     * 删除属性订阅
     * @param userId 用户id
     * @param id 对象id
     * @param attrs 属性列表
     * @return 说明信息
     */
    public String delAttrSubscribe(String userId, String id, List<String> attrs) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.delAttrSubscribe(userId, id, attrs);
        return "删除成功";
    }
    /**
     * 增加实体对象订阅
     * @param userId 用户id
     * @param id 实体对象id
     * @return 说明信息
     */
    public String addObjectSubscribe(String userId, String id) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.addObjectSubscribe(userId, id);
        return "增加成功";
    }

    /**
     * 删除实体对象订阅
     * @param userId 用户id
     * @param id 实体对象id
     * @return 说明信息
     */
    public String delObjectSubscribe(String userId, String id) {
        if(userId == null || userId.equals("")) {
            return "用户id不能为空";
        }
        if(id == null || id.equals("")) {
            return "实体对象id不能为空";
        }
        userDAO.delObjectSubscribe(userId, id);
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
