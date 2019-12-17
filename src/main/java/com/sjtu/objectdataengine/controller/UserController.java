package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.subscribe.UserService;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RequestMapping("/user")
@RestController
public class UserController {
    @Resource
    UserService userService;

    @GetMapping("/register")
    public ResultInterface register(String name, String intro) {
        return userService.register(name, intro);
    }
    @GetMapping("/unregister")
    public ResultInterface unregister(String id) {
        return userService.unregister(id);
    }
}
