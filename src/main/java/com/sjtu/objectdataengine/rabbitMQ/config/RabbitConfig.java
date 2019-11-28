package com.sjtu.objectdataengine.rabbitMQ.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    //Mongo队列 起名：MongoQueue
    @Bean
    public Queue MongoQueue() {
        return new Queue("MongoQueue",true);  //true 是否持久
    }

    //RedisQueue 起名：RedisQueue
    @Bean
    public Queue RedisQueue() {
        return new Queue("RedisQueue", true); //true 是否持久
    }

    //Direct交换机 起名：DataDirectExchange
    @Bean
    DirectExchange DataDirectExchange() {
        return new DirectExchange("DataDirectExchange");
    }

    //绑定  将队列和交换机绑定, 并设置用于匹配键
    @Bean
    Binding bindingExchangeMongo() {
        return BindingBuilder.bind(MongoQueue()).to(DataDirectExchange()).with("MONGO");
    }

    @Bean
    Binding bindingExchangeRedis() {
        return BindingBuilder.bind(RedisQueue()).to(DataDirectExchange()).with("REDIS");
    }
}
