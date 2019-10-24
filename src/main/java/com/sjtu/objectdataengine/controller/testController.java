package com.sjtu.objectdataengine.controller;

import com.mongodb.Mongo;
import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoAttrs;
import com.sjtu.objectdataengine.model.MongoObject;
import com.sjtu.objectdataengine.service.MongoObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RequestMapping("/test")
@RestController
public class testController {
    @Autowired
    private MongoObjectService mongoObjectService;

    @GetMapping("create")
    public boolean testCreate() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("id", "hh");
        hashMap.put("name", "go");
        HashMap<String, Date> objects = new HashMap<>();
        return mongoObjectService.create("2","4", hashMap, objects);
    }

    @GetMapping("find")
    public List<MongoAttrs> find(@RequestParam String id, @RequestParam String name) {
        return mongoObjectService.findAttrsByKey(id, name);
    }

    @GetMapping("find_latest")
    public MongoAttr findLatest(@RequestParam String id, @RequestParam String name) {
        return mongoObjectService.findLatestAttrByKey(id, name);
    }

    @GetMapping("add")
    public boolean add(@RequestParam String id, @RequestParam String name, @RequestParam String value) {
        Date now = new Date();
        MongoAttr mongoAttr = new MongoAttr(value);
        mongoAttr.setCreateTime(now);
        mongoAttr.setUpdateTime(now);
        return mongoObjectService.addValue(id, name, mongoAttr);
    }

    @GetMapping("obj")
    public MongoObject findLatestObject(@RequestParam String id) {
        return mongoObjectService.findLatestObjectByKey(id);
    }

    @GetMapping("time")
    public MongoAttr findByTime(@RequestParam String id, @RequestParam String name, @RequestParam(value ="date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date time) {
        return mongoObjectService.findAttrByTime(id, name, time);
    }

    @GetMapping("time_obj")
    public MongoObject findObjByTime(@RequestParam String id, @RequestParam(value ="date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date time) {
        return mongoObjectService.findObjectByTime(id, time);
    }

    @GetMapping("se")
    public List<MongoAttr> findByStartAndEnd(@RequestParam String id, @RequestParam String name, @RequestParam(value ="start") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date st, @RequestParam(value ="end") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date et) {
        return mongoObjectService.findAttrByStartAndEnd(id, name ,st, et);
    }

    @GetMapping("se_obj")
    public List<MongoObject> findObjByStartAndEnd(@RequestParam String id, @RequestParam(value ="start") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date st, @RequestParam(value ="end") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date et) {
        return mongoObjectService.findObjectByStartAndEnd(id ,st, et);
    }

}
