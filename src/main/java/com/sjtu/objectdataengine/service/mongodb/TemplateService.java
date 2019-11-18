package com.sjtu.objectdataengine.service.mongodb;

import com.sjtu.objectdataengine.rabbitMQ.MongoSender;
import com.sjtu.objectdataengine.service.redis.RedisTemplateService;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TemplateService {

    @Resource
    MongoSender mongoSender;

    @Resource
    RedisTemplateService redisTemplateService;

    public String create() {
        return "创建成功!";
    }
}
