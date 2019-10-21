package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.MongoAttr;
import com.sjtu.objectdataengine.model.MongoAttrs;
import com.sjtu.objectdataengine.service.MongoObjectService;
import org.springframework.beans.factory.annotation.Autowired;
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
        return mongoObjectService.createObject("2","4", hashMap);
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
}
