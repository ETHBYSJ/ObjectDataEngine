package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.TreeNode;
import com.sjtu.objectdataengine.model.TreeNodeReturn;
import com.sjtu.objectdataengine.service.TreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/tree")
@RestController
public class TreeController {
    @Resource
    private TreeService treeService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return treeService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return treeService.delete(id);
    }

    @PostMapping("/modify")
    public String modify(@RequestBody String request) {
        return treeService.modify(request);
    }

    @GetMapping("/get_tree")
    public TreeNodeReturn getTreeByRoot(@RequestParam String id) {
        return treeService.getTreeByRoot(id);
    }

    @GetMapping("/get_node")
    public TreeNode getNodeById(@RequestParam String id) {
        return treeService.getNodeById(id);
    }
}
