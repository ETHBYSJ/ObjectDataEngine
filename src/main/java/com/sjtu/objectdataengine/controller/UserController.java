package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.subscribe.UserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("/create")
    public boolean create(@RequestParam String id, @RequestParam String name, @RequestParam String intro) {
        return userService.create(id, name, intro);
    }
    @GetMapping("/add_template_sub")
    public String addTemplateSub(@RequestParam String userId, @RequestParam String id, @RequestBody List<String> list) {
        return userService.addTemplateSubscribe(userId, id, list);
    }
    @GetMapping("/del_template_sub")
    public String delTemplateSub(@RequestParam String userId, @RequestParam String id) {
        return userService.delTemplateSubscribe(userId, id);
    }
}
