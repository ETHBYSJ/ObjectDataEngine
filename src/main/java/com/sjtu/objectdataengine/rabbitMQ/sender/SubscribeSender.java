package com.sjtu.objectdataengine.rabbitMQ.sender;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class SubscribeSender {

    @Resource
    private RabbitTemplate rabbitTemplate;

    public void send(String message, String routingKey) {
        rabbitTemplate.convertAndSend("SubscribeExchange", routingKey, message);
    }
}
