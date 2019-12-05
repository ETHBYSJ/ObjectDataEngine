package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import com.sjtu.objectdataengine.service.tree.APITreeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/tree")
@RestController
public class TreeController {
    @Resource
    private APITreeService APITreeService;

    @PostMapping("/create")
    @ResponseBody
    public String create(@RequestBody String request) {
        return APITreeService.create(request);
    }

    @GetMapping("/delete")
    public String delete(@RequestParam String id) {
        return APITreeService.delete(id);
    }

    @PostMapping("/modify")
    public String modify(@RequestBody String request) {
        return APITreeService.modify(request);
    }

    @GetMapping("/get_tree")
    public TreeNodeReturn getTreeByRoot(@RequestParam String id) {
        return APITreeService.getTreeByRoot(id);
    }

    @GetMapping("/get_node")
    public TreeNode getNodeById(@RequestParam String id) {
        return APITreeService.getNodeById(id);
    }
}
