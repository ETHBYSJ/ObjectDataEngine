package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.tree.TreeNode;
import com.sjtu.objectdataengine.model.tree.TreeNodeReturn;
import com.sjtu.objectdataengine.service.tree.APITreeService;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
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
    public ResultInterface create(@RequestBody String request) {
        return APITreeService.create(request);
    }

    @GetMapping("/delete")
    public ResultInterface delete(@RequestParam String id) {
        return APITreeService.delete(id);
    }

    @PostMapping("/modify")
    public ResultInterface modify(@RequestBody String request) {
        return APITreeService.modify(request);
    }

    @GetMapping("/get_tree")
    public ResultInterface getTreeByRoot(@RequestParam String id) {
        return APITreeService.getTreeByRoot(id);
    }

    @GetMapping("/get_node")
    public ResultInterface getNodeById(@RequestParam String id) {
        return APITreeService.getNodeById(id);
    }

    @GetMapping("/del_subtree")
    public ResultInterface delSubtree(@RequestParam String id) {
        return APITreeService.delSubtree(id);
    }
}
