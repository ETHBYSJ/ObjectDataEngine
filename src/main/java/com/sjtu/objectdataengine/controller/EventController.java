package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.event.EventObject;
import com.sjtu.objectdataengine.service.event.APIEventService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/event")
@RestController
public class EventController {

    @Resource
    private APIEventService APIEventService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return APIEventService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return APIEventService.delete(id);
    }

    @PostMapping("/modify_base")
    @ResponseBody
    public String modifyBase(@RequestBody String request) {
        return APIEventService.modifyBase(request);
    }

    @GetMapping("/find")
    public EventObject find(@RequestParam String id) {
        return APIEventService.find(id);
    }

    @GetMapping("/end")
    public String end(String id) {
        return APIEventService.end(id);
    }

    @PostMapping("/modify_attr")
    @ResponseBody
    public String modifyAttr(@RequestBody String request) {
        return APIEventService.modifyAttr(request);
    }
}