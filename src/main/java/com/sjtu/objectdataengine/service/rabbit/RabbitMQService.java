package com.sjtu.objectdataengine.service.rabbit;

import com.sjtu.objectdataengine.rabbitMQ.RabbitMQTest.SenderTest;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

@Service
public class RabbitMQService {
    @Autowired
    RabbitAdmin rabbitAdmin;
    @Autowired
    SenderTest senderTest;
    @Resource(name = "testDirectExchange")
    DirectExchange testDirectExchange;
    public void addBinding(Queue queue, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(testDirectExchange).with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }
    public void addBinding(Queue queue, DirectExchange exchange, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }
    public void removeBinding(Binding binding) {
        rabbitAdmin.removeBinding(binding);
    }
    public void sendMessage(String message, String routingKey) {
        senderTest.send(message, routingKey);
    }
    public String addQueue(Queue queue) {
        return rabbitAdmin.declareQueue(queue);
    }
    public void deleteQueue(String queueName, boolean unused, boolean empty) {
        rabbitAdmin.deleteQueue(queueName, unused, empty);
    }
    public boolean deleteQueue(String queueName) {
        return rabbitAdmin.deleteQueue(queueName);
    }
    public void addQueue(String queueName, String routingKey) {
        Queue queue = new Queue(queueName);
        String decl = rabbitAdmin.declareQueue(new Queue(queueName));
        System.out.println(decl);
        addBinding(queue, routingKey);
    }














}
