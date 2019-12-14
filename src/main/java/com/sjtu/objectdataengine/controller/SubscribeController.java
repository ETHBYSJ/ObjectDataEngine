package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.subscribe.SubscribeService;
import com.sjtu.objectdataengine.service.subscribe.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/subscribe")
@RestController
public class SubscribeController {

    @Resource
    SubscribeService subscribeService;
    @Resource
    UserService userService;
    @GetMapping("/register")
    public String register(@RequestParam String name, @RequestParam String intro) {
        return userService.register(name, intro);
    }
    @GetMapping("/create")
    public boolean create(@RequestParam String objId, @RequestParam String type) {
        return subscribeService.create(objId, type);
    }
    /*
    @GetMapping("/add_attr")
    public String addAttrSubscriber(@RequestParam String objId, @RequestParam String type, @RequestParam String name, @RequestParam String userId) {
        return  subscribeService.addAttrSubscriber(objId, type, name, userId);
    }
    */
    /*
    @GetMapping("/del_attr")
    public String delAttrSubscriber(@RequestParam String objId, @RequestParam String type, @RequestParam String name, @RequestParam String userId) {
        return subscribeService.delAttrSubscriber(objId, type, name, userId);
    }
    */
    /*
    @GetMapping("/add")
    public String addObjectSubscriber(@RequestParam String objId, @RequestParam String type, @RequestParam String userId) {
        return  subscribeService.addObjectSubscriber(objId, type, userId);
    }
    */
    /*
    @GetMapping("/del")
    public String delObjectSubscriber(@RequestParam String objId, @RequestParam String type, @RequestParam String userId) {
        return  subscribeService.delObjectSubscriber(objId, type, userId);
    }
    */
}
