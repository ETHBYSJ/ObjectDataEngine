package com.sjtu.objectdataengine.rabbitMQ;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
public class MongoSender {

    @Autowired
    private AmqpTemplate rabbitTemplate;

    public void send(Map message) {
        //System.out.println(message.toString());
        rabbitTemplate.convertAndSend("DataDirectExchange", "MONGO", message);
    }
}
