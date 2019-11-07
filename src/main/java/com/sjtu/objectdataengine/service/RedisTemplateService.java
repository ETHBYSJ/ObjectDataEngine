package com.sjtu.objectdataengine.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.RedisTemplateDAO;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RedisTemplateService {
    @Autowired
    private RedisTemplateDAO redisTemplateDAO;
    private static ObjectMapper MAPPER = new ObjectMapper();
    /**
     * 创建模板
     * @param id 模板id
     * @param name 名字
     * @param type 类型
     * @param nodeId 节点id
     * @param attrs 属性列表
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTemplate(String id, String name, String type, String nodeId, List<String> attrs) {
        //基本属性不能为空
        if(id == null || name == null || type == null || nodeId == null) {
            return false;
        }
        Date now = new Date();
        //创建模板
        //id索引表
        String indexKey = "index";
        String attrsKey = id + '#' + "attrs";
        String baseKey = id + '#' +"base";
        //判断是否是第一次创建
        if(redisTemplateDAO.sHasKey(indexKey, id)) {
            /*
            //不是第一次创建,之前的模板将被修改
            long size = redisTemplateDAO.sGetSetSize(attrsKey);
            redisTemplateDAO.setPop(attrsKey, size);
            redisTemplateDAO.sSet(attrsKey, attrs.toArray());
            */
            return false;
        }
        else {
            //首先存入id索引表
            redisTemplateDAO.sSet(indexKey, id);
            //存储属性列表
            redisTemplateDAO.sSet(attrsKey, attrs.toArray());
        }
        //存储基本信息
        redisTemplateDAO.hset(baseKey, "id", id);
        redisTemplateDAO.hset(baseKey, "name", name);
        redisTemplateDAO.hset(baseKey, "type", type);
        redisTemplateDAO.hset(baseKey, "nodeId", nodeId);
        redisTemplateDAO.hset(baseKey, "createTime", now);
        redisTemplateDAO.hset(baseKey, "updateTime", now);
        //待修改：更新树节点
        return true;
    }

    /**
     * 通过解析请求字符串创建模板
     * @param request 请求
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTemplate(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        //id必须要有
        String id = jsonObject.getString("id");
        if(id == null) return false;
        String name = jsonObject.getString("name");
        if (name == null) name = "";
        String type = jsonObject.getString("type");
        if (type == null) return false;
        String nodeId = jsonObject.getString("nodeId");
        if (nodeId == null) nodeId = "";
        JSONArray jsonArray = jsonObject.getJSONArray("attrs");
        List<String> attrs = jsonArray==null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);

        return createTemplate(id, name, type, nodeId, attrs);
    }

    /**
     * 返回全部模板
     * @return 全部模板
     */
    public List<ObjectTemplate> findAllTemplate() {
        return redisTemplateDAO.findAll();
    }

    /**
     * 根据id返回模板
     * @param id 模板id
     * @return 模板
     */
    public ObjectTemplate findTemplateById(String id) {
        return redisTemplateDAO.findById(id);
    }

    /**
     * 根据id删除模板
     * @param id 模板id
     * @return true代表删除成功，false代表删除失败
     */
    public boolean deleteTemplateById(String id) {
        return redisTemplateDAO.deleteById(id);
    }

    /**
     * 更新类模板
     * @param request 更新请求
     * @return true代表成功，false代表失败
     */
    /*
    public boolean updateTemplate(String request) {
        return false;
    }
    */

}
