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

    @GetMapping("/register")
    public String register(String name, String intro) {
        return userService.register(name, intro);
    }
    @GetMapping("/unregister")
    public boolean unregister(String id) {
        return userService.unregister(id);
    }
}
