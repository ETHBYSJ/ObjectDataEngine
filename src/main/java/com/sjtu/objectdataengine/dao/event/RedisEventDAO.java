package com.sjtu.objectdataengine.dao.event;

import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.model.object.EventObject;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class RedisEventDAO extends RedisDAO {
    private RedisTemplate<String, Object> eventRedisTemplate;
    @Autowired
    public RedisEventDAO(RedisTemplate<String, Object> eventRedisTemplate) {
        super.setTemplate(eventRedisTemplate);
    }

}
