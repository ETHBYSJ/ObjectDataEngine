package com.sjtu.objectdataengine.rabbitMQ.mongodb;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.annotation.Resource;
import java.util.Map;

@Component
public class MongoSender {

    @Resource
    private AmqpTemplate rabbitTemplate;

    public void send(Map message) {
        //System.out.println(message.toString());
        rabbitTemplate.convertAndSend("MongoExchange", "Tree", message);
    }
}
