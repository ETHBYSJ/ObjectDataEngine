package com.sjtu.objectdataengine.rabbitMQ.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Value("${spring.rabbitmq.port}")
    private String port;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    // 连接工厂
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setAddresses(host + ":" + port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    // Mongo队列 起名：MongoQueue
    @Bean
    public Queue MongoQueue() {
        return new Queue("MongoQueue",true);  //true 是否持久
    }

    // RedisQueue 起名：RedisQueue
    @Bean
    public Queue RedisQueue() {
        return new Queue("RedisQueue", true); //true 是否持久
    }

    // ObjectRequestQueue 起名：ObjectRequestQueue
    @Bean
    public Queue ObjectRequestQueue() {
        return new Queue("ObjectRequestQueue", true);
    }

    // Direct交换机 起名：DataDirectExchange
    @Bean
    DirectExchange DataDirectExchange() {
        return new DirectExchange("DataDirectExchange");
    }

    // Subscribe交换机 起名：SubscribeExchange
    @Bean
    DirectExchange SubscribeExchange() {
        return new DirectExchange("SubscribeExchange");
    }

    // Request交换机 起名：RequestExchange
    @Bean
    DirectExchange RequestExchange() {
        return new DirectExchange("RequestExchange");
    }

    // 静态绑定  将队列和交换机绑定, 并设置用于匹配键
    @Bean
    Binding bindingExchangeMongo() {
        return BindingBuilder.bind(MongoQueue()).to(DataDirectExchange()).with("MONGO");
    }

    @Bean
    Binding bindingExchangeRedis() {
        return BindingBuilder.bind(RedisQueue()).to(DataDirectExchange()).with("REDIS");
    }

    @Bean
    Binding bindingExchangeObjectRequest() {
        return BindingBuilder.bind(ObjectRequestQueue()).to(RequestExchange()).with("OBJECT");
    }
}
