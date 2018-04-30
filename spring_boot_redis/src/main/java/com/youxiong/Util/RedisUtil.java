package com.youxiong.Util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisUtil {

    @Autowired
    private static RedisTemplate redisTemplate;

    public static RedisTemplate getSelect_0(){
        JedisConnectionFactory connectionFactory = (JedisConnectionFactory) redisTemplate.getConnectionFactory();
        connectionFactory.setDatabase(0);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }

    public static RedisTemplate getSelect_1(){
        JedisConnectionFactory connectionFactory = (JedisConnectionFactory) redisTemplate.getConnectionFactory();
        connectionFactory.setDatabase(1);
        redisTemplate.setConnectionFactory(connectionFactory);
        return redisTemplate;
    }
}
