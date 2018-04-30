package com.youxiong.web;

import com.youxiong.Util.RedisUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/test")
public class TestController {

    @RequestMapping(value = "/redisSelectOne")
    public String redisSelectOne(){
        RedisTemplate template = RedisUtil.getSelect_0();
        ValueOperations ops = template.opsForValue();
        ops.set("test","0");
        return "success";
    }

    @RequestMapping(value = "/redisSelectTwo")
    public String redisSelectTwo(){
        RedisTemplate template = RedisUtil.getSelect_1();
        ValueOperations ops = template.opsForValue();
        ops.set("test","1");
        return "success";
    }
}
