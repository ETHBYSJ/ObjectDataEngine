package com.sjtu.objectdataengine.rabbitMQ.RabbitMQTest;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SenderTest {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    public void send(String message, String routingKey) {
        rabbitTemplate.convertAndSend("TestDirectExchange", routingKey, message);
    }
}
