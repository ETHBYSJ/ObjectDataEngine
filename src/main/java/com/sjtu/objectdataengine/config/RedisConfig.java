package com.sjtu.objectdataengine.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * redis配置类
 */

@Configuration
@EnableCaching //开启注解
public class RedisConfig extends CachingConfigurerSupport {
    //从配置文件中读取参数
    @Value("${redis.hostName}")
    private String hostName;
    @Value("${redis.port}")
    private int port;
    @Value("${redis.max-idle}")
    private int maxIdle;
    @Value("${redis.min-idle}")
    private int minIdle;
    @Value("${redis.max-wait}")
    private long maxWait;
    @Value("${redis.max-active}")
    private int maxActive;
    @Value("${redis.timeout}")
    private long timeout;
    @Value("${redis.database.attrDB}")
    private int attrDB;
    @Value("${redis.database.objectDB}")
    private int objectDB;
    @Value("${redis.database.templateDB}")
    private int templateDB;
    @Value("${redis.database.treeDB}")
    private int treeDB;

    private void initRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        //使用String作为key的序列化器，使用Jackson作为Value的序列化器
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        //使用Jackson2JsonRedisSerializer来序列化和反序列化redis的value值（默认使用JDK的序列化方式
        Jackson2JsonRedisSerializer jacksonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        //指定要序列化的域，field,get和set,以及修饰符范围，ANY是都有包括private和public
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 指定序列化输入的类型，类必须是非final修饰的，final修饰的类，比如String,Integer等会跑出异常
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jacksonRedisSerializer.setObjectMapper(om);
        // 值采用json序列化
        redisTemplate.setValueSerializer(jacksonRedisSerializer);
        //使用StringRedisSerializer来序列化和反序列化redis的key值
        redisTemplate.setKeySerializer(stringRedisSerializer);

        // 设置hash key 和value序列化模式
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        redisTemplate.setHashValueSerializer(jacksonRedisSerializer);
        redisTemplate.afterPropertiesSet();

    }
    @Bean(name="attrRedisTemplate")
    public RedisTemplate<String, Object> getAttrRedisTemplate() {
        //创建客户端连接
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(attrDB, hostName, port, maxIdle, minIdle, maxActive, maxWait, timeout);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        initRedisTemplate(redisTemplate);
        return redisTemplate;
    }
    @Bean(name="objectRedisTemplate")
    public RedisTemplate<String, Object> getObjectRedisTemplate() {
        //创建客户端连接
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(objectDB, hostName, port, maxIdle, minIdle, maxActive, maxWait, timeout);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        initRedisTemplate(redisTemplate);
        return redisTemplate;
    }
    @Bean(name="templateRedisTemplate")
    public RedisTemplate<String, Object> getTemplateRedisTemplate() {
        //创建客户端连接
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(templateDB, hostName, port, maxIdle, minIdle, maxActive, maxWait, timeout);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        initRedisTemplate(redisTemplate);
        return redisTemplate;
    }
    @Bean(name="treeRedisTemplate")
    public RedisTemplate<String, Object> getTreeRedisTemplate() {
        //创建客户端连接
        LettuceConnectionFactory lettuceConnectionFactory = createLettuceConnectionFactory(treeDB, hostName, port, maxIdle, minIdle, maxActive, maxWait, timeout);
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<String, Object>();
        redisTemplate.setConnectionFactory(lettuceConnectionFactory);
        initRedisTemplate(redisTemplate);
        return redisTemplate;
    }
    private LettuceConnectionFactory createLettuceConnectionFactory(int db, String hostName, int port, int maxIdle, int minIdle, int maxActive, long maxWait, long timeout) {

        //redis配置
        RedisConfiguration redisConfiguration = new RedisStandaloneConfiguration(hostName, port);
        ((RedisStandaloneConfiguration)redisConfiguration).setDatabase(db);
        //连接池配置
        GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
        genericObjectPoolConfig.setMaxIdle(maxIdle);
        genericObjectPoolConfig.setMaxIdle(minIdle);
        genericObjectPoolConfig.setMaxTotal(maxActive);
        genericObjectPoolConfig.setMaxWaitMillis(maxWait);
        //redis客户端配置
        LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder builder = LettucePoolingClientConfiguration.builder().commandTimeout(Duration.ofMillis(timeout));
        builder.poolConfig(genericObjectPoolConfig);
        LettuceClientConfiguration lettuceClientConfiguration = builder.build();
        //根据配置和客户端配置创建连接
        LettuceConnectionFactory lettuceConnectionFactory = new LettuceConnectionFactory(redisConfiguration, lettuceClientConfiguration);
        lettuceConnectionFactory.afterPropertiesSet();

        return lettuceConnectionFactory;
    }
}
