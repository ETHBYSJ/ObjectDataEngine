package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.object.ObjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/")
@RestController
public class DataController {

    @Autowired
    private ObjectService objectService;

    @PostMapping("create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return objectService.create(request);
    }
}
