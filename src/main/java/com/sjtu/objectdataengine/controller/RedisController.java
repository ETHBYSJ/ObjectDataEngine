package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.dao.RedisDAO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 *
 */
@Slf4j
@RequestMapping("/redis")
@RestController
public class RedisController {

    private static int ExpireTime = 60; //redis中存储的过期时间60s

    @Resource
    private RedisDAO redisDAO;

    @PostMapping("set")
    public boolean redisSet(@RequestParam String key, @RequestParam String value) {
        return redisDAO.set(key, value);
    }

    @GetMapping("get")
    public Object redisGet(@RequestParam String key){
        return redisDAO.get(key);
    }

    @PostMapping("expire")
    public boolean expire(@RequestParam String key){
        return redisDAO.expire(key, ExpireTime);
    }
}
