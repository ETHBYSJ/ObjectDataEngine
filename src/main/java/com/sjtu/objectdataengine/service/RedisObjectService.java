package com.sjtu.objectdataengine.service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.RedisDAO;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RedisObjectService {
    @Autowired
    private static ObjectMapper MAPPER;
    @Autowired
    private RedisDAO redisDAO;
    private static final Logger logger = LoggerFactory.getLogger(RedisObjectService.class);
    /**
     * 根据对象id获取对象所有属性
     * @param id 对象id
     * @return 对象属性集合
     */
    public Set<Object> findAttrByObjectId(String id) {
        return redisDAO.sGet(id);
    }

    /**
     * 为指定对象添加属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表插入成功，false代表失败
     */
    public boolean addAttrByObjectId(String id, String attr) {
        //如果属性已存在，插入失败
        if(redisDAO.sHasKey(id, attr)) {
            return false;
        }
        if(redisDAO.sSet(id, attr) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 删除指定对象的属性
     * @param id 对象id
     * @param attr 属性名
     * @return true代表删除成功，false代表失败
     */
    public boolean removeAttrByObjectId(String id, String attr) {
        //如果属性名不存在，删除失败
        if(!redisDAO.sHasKey(id, attr)) {
            return false;
        }
        if(redisDAO.setRemove(id, attr) > 0) {
            return true;
        }
        return false;
    }

    /**
     * 更新指定对象的属性
     * @param id 对象id
     * @param oldAttr 旧属性名
     * @param newAttr 新属性名
     * @return true代表更新成功，false代表更新失败
     */
    public boolean updateAttrByObjectId(String id, String oldAttr, String newAttr) {
        return removeAttrByObjectId(id, oldAttr) && addAttrByObjectId(id, newAttr);
    }

    /**
     *
     * @param request json请求体
     * @return true代表创建成功，false代表创建失败
     */
    public boolean createAttr(String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        //必须传入id
        String id = jsonObject.getString("id");
        if(id == null) return false;
        JSONArray jsonArray = jsonObject.getJSONArray("attr");
        List<String> attr = jsonArray==null ? new ArrayList<>() : JSONObject.parseArray(jsonArray.toJSONString(), String.class);
        return redisDAO.sSet(id, attr.toArray()) > 0;
    }

}
