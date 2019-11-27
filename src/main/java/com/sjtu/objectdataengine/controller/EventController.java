package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.event.EventService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RequestMapping("/event")
@RestController
public class EventController {

    @Resource
    private EventService eventService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return eventService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return eventService.delete(id);
    }

    @PostMapping("/modify_base")
    @ResponseBody
    public String modifyBase(@RequestBody String request) {
        return eventService.modifyBase(request);
    }
}