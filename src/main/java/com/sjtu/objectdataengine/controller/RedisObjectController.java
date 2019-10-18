package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.RedisObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RequestMapping("/redisobject")
@RestController
public class RedisObjectController {

    @Autowired
    private RedisObjectService redisObjectService;

    @GetMapping("find_attr")
    public Set<Object> findAttr(@RequestParam String id) {
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
    @PostMapping("create")
    public boolean createAttr(@RequestBody String request) {
        return redisObjectService.createAttr(request);
    }
}
