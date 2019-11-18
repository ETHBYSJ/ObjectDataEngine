package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.service.mongodb.TemplateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/template")
@RestController
public class TemplateController {

    @Resource
    TemplateService templateService;
}
