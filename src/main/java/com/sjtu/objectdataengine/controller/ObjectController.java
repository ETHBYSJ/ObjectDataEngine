package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.object.CommonObject;
import com.sjtu.objectdataengine.service.object.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RequestMapping("/object")
@RestController
public class ObjectController {

    @Autowired
    private ObjectService objectService;

    @PostMapping("create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return objectService.create(request);
    }
    @GetMapping("find_by_id")
    public CommonObject findById(@RequestParam String id) {
        return objectService.findObjectByKey(id);
    }
    @GetMapping("find_by_time")
    public List<CommonObject> findByTime(@RequestParam String id, @RequestParam Date start, @RequestParam Date end) {
        return objectService.findObjectsByTimes(id, start, end);
    }
}
