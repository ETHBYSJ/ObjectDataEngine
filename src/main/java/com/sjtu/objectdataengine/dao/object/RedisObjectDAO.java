package com.sjtu.objectdataengine.dao.object;

import com.sjtu.objectdataengine.dao.RedisDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisObjectDAO extends RedisDAO {
    private RedisTemplate<String, Object> objectRedisTemplate;
    @Autowired
    public RedisObjectDAO(RedisTemplate<String, Object> objectRedisTemplate) {
        super.setTemplate(objectRedisTemplate);
    }
}