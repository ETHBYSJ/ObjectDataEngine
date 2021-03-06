package com.sjtu.objectdataengine.service.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.dao.tree.MongoTreeDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Component
public class MongoTemplateService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    @Resource
    MongoTreeDAO mongoTreeDAO;

    /**
     * 创建新的对象模板
     * @param id ID
     */
    public void createObjectTemplate(String id, String name, String intro, String nodeId, String type, HashMap<String, String> attrs, Date date) {
        ObjectTemplate t = mongoTemplateDAO.findById(id, ObjectTemplate.class);
        if(t != null) return;
        List<String> objects = new ArrayList<>();
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name, intro, nodeId, type, attrs, objects);
        objectTemplate.setCreateTime(date);
        objectTemplate.setUpdateTime(date);
        if(mongoTemplateDAO.create(objectTemplate)) {
            this.bindToNode(nodeId, id, date);
        }
    }

    /**
     * 返回全部模板
     * @return 全部模板
     */
    public List<ObjectTemplate> findAllTemplate() {
        return mongoTemplateDAO.findAll(ObjectTemplate.class);
    }

    /**
     * 根据Id寻找模板
     * @param key id
     * @return 返回对象模板
     */
    public ObjectTemplate findTemplateById(String key) {
        return mongoTemplateDAO.findById(key, ObjectTemplate.class);
    }


    /**
     * 根据id删除对象模板
     * @param key 对象模板id
     */
    public void deleteTemplateById(String key, String nodeId, Date date) {
        MongoCondition mongoCondition = new MongoCondition();
        if (!nodeId.equals("")) {
            mongoCondition.whereIs("id", nodeId);
            mongoCondition.set("template", "");
            mongoCondition.set("updateTime", date);
            mongoTreeDAO.update(mongoCondition, TreeNode.class);
        }
        mongoTemplateDAO.deleteById(key, ObjectTemplate.class);
    }

    /**
     * 根据条件更新对象模板
     * @param id ID
     */
    public void updateBaseInfo(String id, String name, String intro, Date date){
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.whereIs("id", id);
        if (name != null) mongoCondition.set("name", name);
        if (intro != null) mongoCondition.set("intro", intro);
        mongoCondition.set("updateTime", date);
        mongoTemplateDAO.update(mongoCondition, ObjectTemplate.class);
    }

    public void addAttrs(String id, String name, String nickname, Date date) {
        mongoTemplateDAO.opAttr(id, name, nickname, "add", date);
    }

    public void delAttrs(String id, String name, Date date) {
        mongoTemplateDAO.opAttr(id, name, "del", date);
    }

    /**
     * 添加object
     * @param id 模板id
     * @param objId 对象id
     * @return true or false
     */
    public boolean addObjects(String id, String objId) {
        return mongoTemplateDAO.opObjects(id, objId, "add");
    }

    public boolean delObjects(String id, String objId) {
        return mongoTemplateDAO.opObjects(id, objId, "del");
    }

    private void bindToNode(String nodeId, String template, Date date) {
        MongoCondition mongoCondition = new MongoCondition();
        // 绑定, 把对应nodeId的node上的template改成当前的template
        if (!nodeId.equals("")) {
            mongoCondition.whereIs("id", nodeId);
            mongoCondition.set("template", template);
            mongoCondition.set("updateTime", date);
            mongoTreeDAO.update(mongoCondition, TreeNode.class);
        }
    }
}
