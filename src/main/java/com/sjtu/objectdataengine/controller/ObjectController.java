package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.service.object.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RequestMapping("/object")
@RestController
public class ObjectController {

    @Resource
    private ObjectService objectService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return objectService.create(request);
    }

    @GetMapping("/add_attr")
    public String addAttr(@RequestParam String id, @RequestParam String name, @RequestParam String value) {
        return objectService.addAttr(id, name, value);
    }

    @GetMapping("/get_object_by_id")
    public CommonObject getObjectById(@RequestParam String id) {
        return objectService.findObjectByKey(id);
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