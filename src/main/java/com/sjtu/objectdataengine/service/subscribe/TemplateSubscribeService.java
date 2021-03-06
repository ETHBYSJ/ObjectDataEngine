package com.sjtu.objectdataengine.service.subscribe;

import com.sjtu.objectdataengine.dao.subscribe.TemplateSubscribeDAO;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.subscribe.TemplateBaseSubscribeMessage;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class TemplateSubscribeService {

    @Resource
    TemplateSubscribeDAO templateSubscribeDAO;

    @Resource
    MongoTemplateDAO mongoTemplateDAO;


    /**
     * 根据模板id删除订阅表
     * @param id 模板id
     * @return true or false
     */
    public boolean deleteById(String id) {
        // 检查统一放在上层
        return templateSubscribeDAO.deleteById(id + "template", TemplateBaseSubscribeMessage.class);
    }

    /**
     * 根据id创建订阅表
     * @param id 模板id
     * @return true or false
     */
    public boolean create(String id, Date date) {
        if(this.findById(id) != null) return false;
        TemplateBaseSubscribeMessage templateSubscribeMessage = new TemplateBaseSubscribeMessage(id, "template");
        if(mongoTemplateDAO.findById(id, ObjectTemplate.class) == null) {
            return false;
        }
        templateSubscribeMessage.setCreateTime(date);
        templateSubscribeMessage.setUpdateTime(date);
        return templateSubscribeDAO.create(templateSubscribeMessage);
    }

    /**
     * 增加订阅者
     * @param id 模板id
     * @param userId 用户id
     * @return true or false
     */
    public boolean addTemplateSubscriber(String id, String userId, List<String> events) {
        return templateSubscribeDAO.addObjectSubscriber(id, "template", userId, events);
    }

    /**
     * 删除订阅者
     * @param id 模板id
     * @param userId 用户ID
     * @return true or false
     */
    public boolean delTemplateSubscriber(String id, String userId) {
        return templateSubscribeDAO.delObjectSubscriber(id, "template", userId);
    }

    /**
     * 根据id查找订阅消息
     * @param id 模板id
     * @return 订阅消息
     */
    public TemplateBaseSubscribeMessage findById(String id) {
        return templateSubscribeDAO.findById(id + "template", TemplateBaseSubscribeMessage.class);
    }
}
