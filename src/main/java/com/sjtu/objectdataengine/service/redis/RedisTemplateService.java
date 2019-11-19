package com.sjtu.objectdataengine.service.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.RedisTemplateDAO;
import com.sjtu.objectdataengine.dao.RedisTreeDAO;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RedisTemplateService {
    @Autowired
    private RedisTemplateDAO redisTemplateDAO;
    @Autowired
    private RedisTreeDAO redisTreeDAO;
    /**
     * 创建模板
     * @param id 模板id
     * @param name 名字
     * @param type 类型
     * @param nodeId 节点id
     * @param attrs 属性列表
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createTemplate(String id, String name, String type, String nodeId, HashMap<String, String> attrs) {
        Date now = new Date();
        //创建模板
        //id索引表
        String indexKey = "index";
        String attrsKey = id + '#' + "attrs";
        String baseKey = id + '#' +"base";
        //判断是否是第一次创建
        if(redisTemplateDAO.sHasKey(indexKey, id)) {
            return false;
        }
        //首先存入id索引表
        redisTemplateDAO.sSet(indexKey, id);
        if(attrs != null && attrs.size() > 0) {
            //存储属性列表
            redisTemplateDAO.hmset(attrsKey, attrs);
        }

        //存储基本信息
        redisTemplateDAO.hset(baseKey, "id", id);
        if(!name.equals("")) redisTemplateDAO.hset(baseKey, "name", name);
        if(!type.equals("")) redisTemplateDAO.hset(baseKey, "type", type);
        if(!nodeId.equals("")) redisTemplateDAO.hset(baseKey, "nodeId", nodeId);
        //如果树节点已经存在，建立树节点与模板的关联
        if(redisTreeDAO.sHasKey("index", nodeId)) {
            redisTreeDAO.hset(nodeId + "#base", "template", id);
        }
        redisTemplateDAO.hset(baseKey, "createTime", now);
        redisTemplateDAO.hset(baseKey, "updateTime", now);
        return true;
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
        //解除树节点的绑定
        Object nodeId = redisTemplateDAO.hget(id + "#base", "nodeId");
        if(nodeId != null) {
            redisTreeDAO.hset(nodeId + "#base", "template", "");
            redisTreeDAO.hset(nodeId + "#base", "updateTime", new Date());
        }
        return redisTemplateDAO.deleteById(id);
    }

    public boolean hasKey(String id) {
        return redisTemplateDAO.sHasKey("index", id);
    }

}
