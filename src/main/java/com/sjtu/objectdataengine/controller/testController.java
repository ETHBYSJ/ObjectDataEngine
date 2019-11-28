package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.utils.MongoAttr;
import com.sjtu.objectdataengine.model.object.MongoAttrs;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.service.object.MongoObjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RequestMapping("/test")
@RestController
public class testController {
    @Resource
    private MongoObjectService mongoObjectService;

    @GetMapping("find")
    public List<MongoAttrs> find(@RequestParam String id, @RequestParam String name) {
        return mongoObjectService.findAttrsByKey(id, name);
    }

    @GetMapping("find_latest")
    public MongoAttr findLatest(@RequestParam String id, @RequestParam String name) {
        return mongoObjectService.findLatestAttrByKey(id, name);
    }

    @GetMapping("obj")
    public CommonObject findLatestObject(@RequestParam String id) {
        return mongoObjectService.findLatestObjectByKey(id);
    }

    @GetMapping("time")
    public MongoAttr findByTime(@RequestParam String id, @RequestParam String name, @RequestParam(value ="date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date time) {
        return mongoObjectService.findAttrByTime(id, name, time);
    }

    @GetMapping("time_obj")
    public CommonObject findObjByTime(@RequestParam String id, @RequestParam(value ="date") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date time) {
        return mongoObjectService.findObjectByTime(id, time);
    }

    @GetMapping("se")
    public List<MongoAttr> findByStartAndEnd(@RequestParam String id, @RequestParam String name, @RequestParam(value ="start") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date st, @RequestParam(value ="end") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date et) {
        return mongoObjectService.findAttrByStartAndEnd(id, name ,st, et);
    }

    @GetMapping("se_obj")
    public List<CommonObject> findObjByStartAndEnd(@RequestParam String id, @RequestParam(value ="start") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date st, @RequestParam(value ="end") @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") Date et) {
        return mongoObjectService.findObjectByStartAndEnd(id ,st, et);
    }

}
