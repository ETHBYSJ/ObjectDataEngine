package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.mongodb.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/template")
@RestController
public class TemplateController {

    @Resource
    TemplateService templateService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return templateService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return templateService.delete(id);
    }

    @PostMapping("/modify")
    public String modify(@RequestBody String request) {
        return templateService.modify(request);
    }
}
