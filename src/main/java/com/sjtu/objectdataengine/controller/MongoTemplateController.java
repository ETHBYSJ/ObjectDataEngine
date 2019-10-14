package com.sjtu.objectdataengine.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sjtu.objectdataengine.dao.MongoTemplateDAO;
import com.sjtu.objectdataengine.model.ObjectTemplate;
import com.sjtu.objectdataengine.utils.MongoCondition;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RequestMapping("/template")
@RestController
public class MongoTemplateController {

    private static ObjectMapper MAPPER = new ObjectMapper();

    @Resource
    private MongoTemplateDAO mongoTemplateDAO;

    @GetMapping("findbykey")
    public ObjectTemplate findByKey(@RequestParam String key) {
        return mongoTemplateDAO.findByKey(key);
    }

    @GetMapping("find")
    public List<ObjectTemplate> find (@RequestParam String query, @RequestParam String update) throws Exception {
        HashMap<String, String> queryMap = MAPPER.readValue(query, HashMap.class);
        HashMap<String, String> updateMap = MAPPER.readValue(update, HashMap.class);
        MongoCondition mongoCondition = new MongoCondition("query", queryMap, updateMap);
        return mongoTemplateDAO.findByArgs(mongoCondition);
    }

    @PostMapping("create")
    public void create(@RequestParam String id, @RequestParam String name, @RequestParam List<String> attr) {
        Set<String> attrSet = new HashSet<String>(attr);
        ObjectTemplate objectTemplate = new ObjectTemplate(id, name, attrSet);
        mongoTemplateDAO.create(objectTemplate);
    }

    @PostMapping("delbykey")
    public void delByKey(@RequestParam String key) {
        mongoTemplateDAO.deleteByKey(key);
    }


}
