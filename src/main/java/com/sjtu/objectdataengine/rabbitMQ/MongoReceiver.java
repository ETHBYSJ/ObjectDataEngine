package com.sjtu.objectdataengine.rabbitMQ;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = "MongoQueue")//监听的队列名称 MongoQueue
public class MongoReceiver {
    @RabbitHandler
    public void process(Map testMessage) {
        System.out.println("c"+Thread.currentThread().getId());
        System.out.println("DirectReceiver消费者收到消息  : " + testMessage.toString());
    }
}
