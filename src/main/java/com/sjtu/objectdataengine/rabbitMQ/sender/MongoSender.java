package com.sjtu.objectdataengine.rabbitMQ.sender;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class MongoSender {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(Map message) {
        //System.out.println(message.toString());
        rabbitTemplate.convertAndSend("DataDirectExchange", "MONGO", message);
    }
}
