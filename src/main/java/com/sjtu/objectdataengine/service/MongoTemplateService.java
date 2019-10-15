package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Component
public class MongoTemplateService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    MongoTemplateDAO mongoTemplateDAO;

    /**
     * 创建新的对象模板
     * @param request json请求体
     * @return true表示成功创建，false反之
     */
    public boolean createObjectTemplate(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        JSONArray jsonArray = jsonObject.getJSONArray("attr");
        List<String> attr = JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        HashSet<String> attrSet = new HashSet<String>(attr);
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name, attrSet);
        return mongoTemplateDAO.create(objectTemplate);
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
        HashMap<String, String> queryMap = MAPPER.readValue(request, HashMap.class);
        MongoCondition mongoCondition = new MongoCondition("query", queryMap, queryMap);
        return mongoTemplateDAO.findByArgs(mongoCondition);
    }

    /**
     * 根据id删除对象模板
     * @param key 对象模板id
     * @return true表示成功，false反之
     */
    public boolean deleteTemplateById(String key) {
        return mongoTemplateDAO.deleteByKey(key);
    }

    /**
     * 根据条件删除对象模板
     * @param request 删除条件
     * @return 返回true表示成功，false反之
     * @throws Exception readValue错误
     */
    public boolean deleteTemplate(String request) throws Exception{
        HashMap<String, String> queryMap = MAPPER.readValue(request, HashMap.class);
        MongoCondition mongoCondition = new MongoCondition("delete", queryMap, queryMap);
        return mongoTemplateDAO.deleteByArgs(mongoCondition);
    }

    public boolean updateTemplate(String request) throws Exception{
        //System.out.println(query);
        JSONObject jsonObject = JSON.parseObject(request);
        String query = jsonObject.getJSONObject("query").toJSONString();
        String update = jsonObject.getJSONObject("update").toJSONString();
        HashMap<String, String> queryMap = MAPPER.readValue(query, HashMap.class);
        HashMap<String, String> updateMap = MAPPER.readValue(update, HashMap.class);
        MongoCondition mongoCondition = new MongoCondition("update", queryMap, updateMap);
        //System.out.println(query);
        //System.out.println(update);
        return mongoTemplateDAO.update(mongoCondition);
    }
}
