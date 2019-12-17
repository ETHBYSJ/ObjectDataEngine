package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.object.MongoObjectDAO;
import com.sjtu.objectdataengine.dao.subscribe.UserDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.subscribe.EntityBaseSubscribeMessage;
import com.sjtu.objectdataengine.model.subscribe.TemplateBaseSubscribeMessage;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import com.sjtu.objectdataengine.utils.Result.Result;
import com.sjtu.objectdataengine.utils.Result.ResultCodeEnum;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;

@Component
public class SubscribeService {
    // 订阅总服务，联系各订阅分服务和用户服务
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

    public ResultInterface deleteByIdAndType(String id, String type) {
        if(type.equals("template")) {
            TemplateBaseSubscribeMessage templateSubscribeMessage = templateSubscribeService.findById(id);
            if(templateSubscribeMessage == null) return Result.build(ResultCodeEnum.SUB_MSG_DEL_TEMPLATE_NOT_FOUND);
            // 从用户表中删除
            Set<String> templateSubscriber = templateSubscribeMessage.getTemplateSubscriber().keySet();
            for(String user : templateSubscriber) {
                userService.delTemplateSubscribe(user, id);
            }
            // 从订阅表中删除
            if(templateSubscribeService.deleteById(id)) {
                return Result.build(ResultCodeEnum.SUB_MSG_DEL_TEMPLATE_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_MSG_DEL_TEMPLATE_FAIL);
        }
        else if(type.equals("entity")) {
            EntityBaseSubscribeMessage entitySubscribeMessage = entitySubscribeService.findById(id);
            if(entitySubscribeMessage == null) return Result.build(ResultCodeEnum.SUB_MSG_DEL_ENTITY_NOT_FOUND);
            // 从用户表中删除
            List<String> objectSubscriber = entitySubscribeMessage.getObjectSubscriber();
            for(String user : objectSubscriber) {
                userService.delObjectSubscribe(user, id);
            }
            HashMap<String, List<String>> attrsSubscriber = entitySubscribeMessage.getAttrsSubscriber();
            for(Map.Entry<String, List<String>> entry : attrsSubscriber.entrySet()) {
                String name = entry.getKey();
                List<String> attrSubscriber = entry.getValue();
                for(String user : attrSubscriber) {
                    userService.delAttrSubscribe(user, id, name);
                }
            }
            // 从订阅表中删除
            if(entitySubscribeService.deleteById(id)) {
                return Result.build(ResultCodeEnum.SUB_MSG_DEL_ENTITY_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_MSG_DEL_ENTITY_FAIL);
        }
        else return Result.build(ResultCodeEnum.SUB_MSG_DEL_TYPE_ERROR);
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

    public ResultInterface addEntitySubscriber(String id, String user, List<String> attrs) {
        // 检测空
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_EMPTY_ID);
        if (user == null || user.equals("")) return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_EMPTY_USER_ID);
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_USER_NOT_FOUND);
        }
        // 1.检查对象是否存在
        if(objectService.findObjectById(id) == null) {
            return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_NOT_FOUND);
        }
        // 2.订阅表如果不存在，创建订阅表
        if(entitySubscribeService.findById(id) == null) {
            entitySubscribeService.create(id, date);
        }
        // 双向操作
        if(attrs == null || attrs.size() == 0) {
            if(entitySubscribeService.addEntitySubscriber(id, user)) {
                userService.addObjectSubscribe(user, id);
                return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_FAIL);
        }
        else {
            if(entitySubscribeService.addEntityAttrSubscriber(id, attrs, user)) {
                userService.addAttrSubscribe(user, id, attrs);
                return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_ADD_ENTITY_FAIL);
        }
    }

    /**
     * 删除对象订阅者
     * @param user 订阅者id
     * @return 结果说明
     */
    public ResultInterface delEntitySubscriber(String id, String user, List<String> attrs) {
        // 检测空
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_EMPTY_ID);
        if (user == null || user.equals("")) return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_EMPTY_USER_ID);
        // 检测用户
        if(!userService.hasUser(user)) {
            return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_USER_NOT_FOUND);
        }
        // 双向操作
        if(attrs == null || attrs.size() == 0) {
            if(entitySubscribeService.delEntitySubscriber(id, user)) {
                userService.delObjectSubscribe(user, id);
                return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_FAIL);
        }
        else {
            if(entitySubscribeService.delEntityAttrSubscriber(id, attrs, user)) {
                userService.delAttrSubscribe(user, id, attrs);
                return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_SUCCESS);
            }
            return Result.build(ResultCodeEnum.SUB_DEL_ENTITY_FAIL);
        }
    }

    /**
     *
     * @param id 模板id
     * @param user 用户id
     * @param list 绑定事件列表
     * @return 说明消息
     */
    public ResultInterface addTemplateSubscriber(String id, String user, List<String> list) {
        // 检测空
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_EMPTY_ID);
        if (user == null || user.equals("")) return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_EMPTY_USER_ID);
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_USER_NOT_FOUND);
        }
        // 1.检查对象是否存在
        if(templateService.get(id) == null) {
            return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_NOT_FOUND);
        }
        // 2.订阅表如果不存在，创建订阅表
        if(templateSubscribeService.findById(id) == null) {
            templateSubscribeService.create(id, date);
        }
        // 双向操作
        if(templateSubscribeService.addTemplateSubscriber(id, user, list)) {
            userService.addTemplateSubscribe(user, id, list);
            return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_SUCCESS);
        }
        return Result.build(ResultCodeEnum.SUB_ADD_TEMPLATE_FAIL);
    }

    /**
     *
     * @param id 模板id
     * @param user 用户id
     * @return 说明消息
     */
    public ResultInterface delTemplateSubscriber(String id, String user) {
        // 检测空
        if(id == null || id.equals("")) return Result.build(ResultCodeEnum.SUB_DEL_TEMPLATE_EMPTY_ID);
        if (user == null || user.equals("")) return Result.build(ResultCodeEnum.SUB_DEL_TEMPLATE_EMPTY_USER_ID);
        Date date = new Date();
        // 检测用户
        if(!userService.hasUser(user)) {
            return Result.build(ResultCodeEnum.SUB_DEL_TEMPLATE_USER_NOT_FOUND);
        }
        // 双向操作
        if(templateSubscribeService.delTemplateSubscriber(id, user)) {
            userService.delTemplateSubscribe(user, id);
            return Result.build(ResultCodeEnum.SUB_DEL_TEMPLATE_SUCCESS);
        }
        return Result.build(ResultCodeEnum.SUB_DEL_TEMPLATE_FAIL);
    }

}
