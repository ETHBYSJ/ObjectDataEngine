package com.sjtu.objectdataengine.rabbitMQ.outside.sender;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class SubscribeSender {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(Map message, String routingKey) {
        rabbitTemplate.convertAndSend("SubscribeExchange", routingKey, message);
    }
}
