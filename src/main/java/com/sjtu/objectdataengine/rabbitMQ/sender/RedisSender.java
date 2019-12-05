package com.sjtu.objectdataengine.rabbitMQ.sender;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RedisSender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(Map message) {
        rabbitTemplate.convertAndSend("DataDirectExchange", "REDIS", message);
    }
}
