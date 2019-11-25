package com.sjtu.objectdataengine.service.template;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.template.MongoTemplateDAO;
import com.sjtu.objectdataengine.dao.tree.MongoTreeDAO;
import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    public void createObjectTemplate(String id, String name, String intro, String nodeId, String type, HashMap<String, String> attrs) {
        HashMap<String, String> objects = new HashMap<>();
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name, intro, nodeId, type, attrs, objects);
        if(mongoTemplateDAO.create(objectTemplate)) {
            this.bindToNode(nodeId, id);
        }
    }

    /**
     * 返回全部模板
     * @return 全部模板
     */
    public List<ObjectTemplate> findAllTemplate() {
        return mongoTemplateDAO.findAll();
    }

    /**
     * 根据Id寻找模板
     * @param key id
     * @return 返回对象模板
     */
    public ObjectTemplate findTemplateById(String key) {
        return mongoTemplateDAO.findByKey(key);
    }

    /**
     * 根据条件查询类模板
     * @param request 查询条件，传过来的时候是JSON格式
     * @return 返回查询结果
     * @throws Exception readValue
     */
    public List<ObjectTemplate> findTemplate(String request) throws Exception{
        HashMap<String, Object> queryMap = MAPPER.readValue(request, HashMap.class);
        MongoCondition mongoCondition = new MongoCondition("query", queryMap, queryMap);
        return mongoTemplateDAO.findByArgs(mongoCondition);
    }

    /**
     * 根据id删除对象模板
     * @param key 对象模板id
     */
    public void deleteTemplateById(String key, String nodeId) {
        MongoCondition mongoCondition = new MongoCondition();
        if (!nodeId.equals("")) {
            mongoCondition.addQuery("id", nodeId);
            mongoCondition.addUpdate("template", "");
            mongoTreeDAO.update(mongoCondition);
        }
        mongoTemplateDAO.deleteByKey(key);
    }

    /**
     * 根据条件更新对象模板
     * @param id ID
     */
    public void updateBaseInfo(String id, String name, String intro){
        MongoCondition mongoCondition = new MongoCondition();
        mongoCondition.addQuery("id", id);
        if (name != null) mongoCondition.addUpdate("name", name);
        if (intro != null) mongoCondition.addUpdate("intro", intro);
        mongoTemplateDAO.update(mongoCondition);
    }

    public void addAttrs(String id, String name, String nickname) {
        mongoTemplateDAO.opAttr(id, name, nickname, "add");
    }

    public void delAttrs(String id, String name) {
        mongoTemplateDAO.opAttr(id, name, "del");
    }

    public void addObjects(String id, String objId, String name) {

    }

    public void delObjects(String id, String objId) {

    }
    private void bindToNode(String nodeId, String template) {
        MongoCondition mongoCondition = new MongoCondition();
        // 绑定, 把对应nodeId的node上的template改成当前的template
        if (!nodeId.equals("")) {
            mongoCondition.addQuery("id", nodeId);
            mongoCondition.addUpdate("template", template);
            mongoCondition.addUpdate("updateTime", new Date());
            mongoTreeDAO.update(mongoCondition);
        }
    }
}
