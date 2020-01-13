package com.sjtu.objectdataengine.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.service.object.APIObjectService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RequestMapping("/object")
@RestController
public class ObjectController {

    @Resource
    private APIObjectService objectService;
    @GetMapping("/bind")
    public String bindEventAndObject(@RequestParam String objId, @RequestParam String eventId) {
        return objectService.bindEventAndObject(objId, eventId);
    }
    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return objectService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return objectService.deleteObjectById(id);
    }

    @PostMapping("/add_attr")
    @ResponseBody
    public String addAttrPost(@RequestBody String request) {
        JSONObject jsonObject = JSON.parseObject(request);
        String id = jsonObject.getString("id");
        String name = jsonObject.getString("name");
        String value = jsonObject.getJSONArray("value").toJSONString();

        return addAttr(id, name, value);
    }

    public String addAttr(String id, String name, String value) {
        return objectService.addAttr(id, name, value);
    }
    @GetMapping("/addAttribute")
    public String addAttribute(@RequestParam String id, @RequestParam String name, @RequestParam String value) {
        return objectService.addAttr(id, name, value);
    }
    @GetMapping("/get_object_by_id")
    public CommonObject getObjectById(@RequestParam String id) {
        return objectService.findObjectById(id);
    }

    @GetMapping("/get_object_by_date")
    public CommonObject getObjectByDate(@RequestParam String id, @RequestParam(value = "date") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date date) {
        return objectService.findObjectByTime(id, date);
    }

    @GetMapping("/get_objects_by_date")
    public List<CommonObject> getObjectsByDate(@RequestParam String id, @RequestParam(value = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date st, @RequestParam(value = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date et) {
        return objectService.findObjectsByTimes(id, st, et);
    }

    @GetMapping("/get_object_by_event")
    public List<CommonObject> getObjectByEvent(@RequestParam String nodeId, @RequestParam String eventId) {
        return objectService.findObjectsByNodeAndEvent(nodeId, eventId);
    }
}