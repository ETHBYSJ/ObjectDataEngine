package com.sjtu.objectdataengine.dao.event;

import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.utils.MongoAttr;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class RedisEventAttrDAO extends RedisDAO {
    private RedisTemplate<String, Object> eventRedisTemplate;
    @Autowired
    public RedisEventAttrDAO(RedisTemplate<String, Object> eventAttrRedisTemplate) {
        super.setTemplate(eventAttrRedisTemplate);
    }
    public MongoAttr findAttr(String id, String name) {
        String key = id + '#' + name;
        Date attrCreateTime = (Date) hget(key, "createTime");
        Date attrUpdateTime = (Date) hget(key, "updateTime");
        String value = (String) hget(key, "value");
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setUpdateTime(attrUpdateTime);
        return mongoAttr;
    }
}

