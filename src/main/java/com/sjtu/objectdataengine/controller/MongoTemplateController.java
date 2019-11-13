package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.service.mongodb.MongoTemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/template")
@RestController
public class MongoTemplateController {


    @Resource
    private MongoTemplateService mongoTemplateService;

    @PostMapping("create")
    @ResponseBody
    public boolean create(@RequestBody String request) {
        return mongoTemplateService.createObjectTemplate(request);
    }

    @GetMapping("find_all")
    public  List<ObjectTemplate> findAll() {
        return mongoTemplateService.findAllTemplate();
    }

    @GetMapping("find_by_key")
    public ObjectTemplate findByKey(@RequestParam String key) {
        return mongoTemplateService.findTemplateById(key);
    }

    @PostMapping("find")
    @ResponseBody
    public List<ObjectTemplate> find (@RequestBody String request) throws Exception {
        return mongoTemplateService.findTemplate(request);
    }

    @GetMapping("delete_by_key")
    public boolean delByKey(@RequestParam String key) {
        return mongoTemplateService.deleteTemplateById(key);
    }

    @PostMapping("delete")
    @ResponseBody
    public boolean delete (@RequestBody String request) throws Exception{
        return mongoTemplateService.deleteTemplate(request);
    }

    @PostMapping("update")
    @ResponseBody
    public boolean update(@RequestBody String query) throws Exception {
        return mongoTemplateService.updateTemplate(query);
    }
}
