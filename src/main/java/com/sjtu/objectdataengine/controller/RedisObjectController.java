package com.sjtu.objectdataengine.controller;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.service.RedisObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequestMapping("/redisobject")
@RestController
public class RedisObjectController {
    /*
    @Autowired
    private RedisDAO redisDAO;
    */
    @GetMapping("zadd")
    public boolean Zadd(@RequestParam String id, @RequestParam String attr, @RequestParam String value, @RequestParam Date date) {
        return redisObjectService.Zadd(id, attr, value, date);
    }
    @GetMapping("get_latest")
    public JSONObject getLatest(@RequestParam String id) {
        return redisObjectService.findObjectById(id);
    }
    @GetMapping("get_by_time")
    public JSONObject getByTime(@RequestParam String id, @RequestParam Date date) {
        return redisObjectService.findObjectById(id, date);
    }
    @Autowired
    private RedisObjectService redisObjectService;

    @GetMapping("find_attr")
    public List<Object> findAttr(@RequestParam String id) {
        return redisObjectService.findAttrByObjectId(id);
    }

    @GetMapping("remove_attr")
    public boolean removeAttr(@RequestParam String id, @RequestParam String attr) {
        return redisObjectService.removeAttrByObjectId(id, attr);
    }
    @GetMapping("add_attr")
    public boolean addAttr(@RequestParam String id, @RequestParam String attr) {
        return redisObjectService.addAttrByObjectId(id, attr);
    }
    @GetMapping("update_attr")
    public boolean updateAttr(@RequestParam String id, @RequestParam String oldAttr, @RequestParam String newAttr) {
        return redisObjectService.updateAttrByObjectId(id, oldAttr, newAttr);
    }
    @PostMapping("create_attr")
    public boolean createAttr(@RequestBody String request) {
        return redisObjectService.createAttr(request);
    }

}
