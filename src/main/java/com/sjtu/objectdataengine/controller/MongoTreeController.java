package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.KnowledgeTreeNode;
import com.sjtu.objectdataengine.service.MongoTreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/tree")
@RestController
public class MongoTreeController {

    @Resource
    private MongoTreeService mongoTreeService;

    @PostMapping("/create")
    @ResponseBody
    public boolean create(@RequestBody String request) {
        return mongoTreeService.createTreeNode(request);
    }

    @GetMapping("/delete_node")
    public boolean deleteNode(@RequestParam String key) {
        return mongoTreeService.deleteWholeNodeByKey(key);
    }

    @GetMapping("/find_node")
    public KnowledgeTreeNode findNode(@RequestParam String key) {
        return mongoTreeService.findNodeByKey(key);
    }

}
