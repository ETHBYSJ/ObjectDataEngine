package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.model.template.ObjectTemplate;
import com.sjtu.objectdataengine.service.template.APITemplateService;
import com.sjtu.objectdataengine.utils.Result.ResultInterface;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/template")
@RestController
public class TemplateController {

    @Resource
    APITemplateService APITemplateService;

    @PostMapping("/create")
    @ResponseBody
    public ResultInterface create(@RequestBody String request) {
        return APITemplateService.create(request);
    }

    @GetMapping("/delete")
    public ResultInterface delete(@RequestParam String id) {
        return APITemplateService.delete(id);
    }

    @PostMapping("/modify_base")
    public ResultInterface modifyBaseInfo(@RequestBody String request) {
        return APITemplateService.modifyBaseInfo(request);
    }

    @PostMapping("/add_attr")
    @ResponseBody
    public ResultInterface addAttr(@RequestBody String request) {
        return APITemplateService.addAttr(request);
    }

    @GetMapping("/del_attr")
    public ResultInterface delAttr(@RequestParam String id, @RequestParam String name) {
        return APITemplateService.delAttr(id, name);
    }
    @GetMapping("/get")
    public ResultInterface get(@RequestParam String id) {
        return APITemplateService.get(id);
    }
}
