package com.sjtu.objectdataengine.controller;

import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.dao.RedisDAO;
import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.service.RedisObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@RequestMapping("/redisobject")
@RestController
public class RedisObjectController {
    @Autowired
    private RedisObjectService redisObjectService;
    @GetMapping("create_obj")
    public boolean testCreateObject() throws Exception {
        HashMap<String, Date> objects = new HashMap<String, Date>();
        objects.put("11", new Date());
        objects.put("22", new Date());
        HashMap<String, MongoAttr> attrs = new HashMap<String, MongoAttr>();
        MongoAttr name = new MongoAttr("a");
        MongoAttr age = new MongoAttr("1");
        name.setCreateTime(new Date());
        name.setUpdateTime(new Date());
        age.setCreateTime(new Date());
        age.setUpdateTime(new Date());
        attrs.put("name", name);
        attrs.put("age", age);
        Date createTime = new Date();
        Date updateTime = new Date();
        return redisObjectService.createObject("2", "4", "3", "object", createTime, updateTime, attrs, objects);
    }
    @GetMapping("zadd")
    public boolean Zadd(@RequestParam String id, @RequestParam String attr, @RequestParam String value, @RequestParam Date date) {
        return redisObjectService.Zadd(id, attr, value, date);
    }
    @GetMapping("get_latest")
    public MongoObject getLatest(@RequestParam String id) {
        return redisObjectService.findObjectById(id);
    }
    @GetMapping("get_by_time")
    public MongoObject getByTime(@RequestParam String id, @RequestParam Date date) {
        return redisObjectService.findObjectById(id, date);
    }
    @GetMapping("get_by_time_interval")
    public List<MongoObject> getByTimeInterval(@RequestParam String id, @RequestParam Date startDate, @RequestParam Date endDate) {
        return redisObjectService.findObjectBetweenTime(id, startDate, endDate);
    }


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
