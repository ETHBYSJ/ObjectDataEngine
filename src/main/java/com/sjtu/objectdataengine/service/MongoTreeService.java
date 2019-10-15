package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import com.sjtu.objectdataengine.dao.MongoTreeDAO;
import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.utils.MongoCondition;

import javax.annotation.Resource;
import java.util.List;

public class MongoTreeService {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    MongoTreeDAO mongoTreeDAO;

    private void addChild(String child, List<String> parents) {
        MongoCondition mongoCondition = new MongoCondition();
        for (String parent : parents) {
            List<String> childrenList = mongoTreeDAO.findByKey(parent).getChildren();
            childrenList.add(child);
            mongoCondition.addQuery("_id", parent);
            mongoCondition.addUpdate("children", childrenList);
            mongoTreeDAO.update(mongoCondition);
        }
    }

    public boolean createTreeNode(String request) {
        //解析
        JSONObject jsonObject = JSON.parseObject(request);
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        String template = jsonObject.getString("template");
        JSONArray jsonArray = jsonObject.getJSONArray("parents");
        List<String> parents = JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        KnowledgeTreeNode knowledgeTreeNode = new KnowledgeTreeNode(id, name, template, parents);
        try { //要保证原子性
            mongoTreeDAO.create(knowledgeTreeNode);
            addChild(id, parents);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    //public boolean deleteBy
}
