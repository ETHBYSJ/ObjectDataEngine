package com.sjtu.objectdataengine.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisAttrDAO extends RedisDAO {
    private RedisTemplate<String, Object> attrRedisTemplate;
    @Autowired
    public RedisAttrDAO(RedisTemplate<String, Object> attrRedisTemplate) {
        super.setTemplate(attrRedisTemplate);
    }
}
