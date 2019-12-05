package com.sjtu.objectdataengine.service.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class RabbitMQService {

    @Resource
    RabbitAdmin rabbitAdmin;

    @Resource(name = "SubscribeExchange")
    DirectExchange subscribeExchange;

    private void addBinding(Queue queue, DirectExchange exchange, String routingKey) {
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routingKey);
        rabbitAdmin.declareBinding(binding);
    }

    /**
     * 增加一个队列
     * @param queueName 队列名称
     * @param routingKey 路由键
     */
    public void addQueue(String queueName, String routingKey) {
        Queue queue = new Queue(queueName);
        rabbitAdmin.declareQueue(new Queue(queueName));
        this.addBinding(queue, subscribeExchange, routingKey);
    }

    /**
     * 删除一个队列
     * @param queueName 队列名称
     */

    public void delQueue(String queueName) {
        rabbitAdmin.deleteQueue(queueName);
    }














}
