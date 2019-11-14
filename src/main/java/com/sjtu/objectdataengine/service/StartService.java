package com.sjtu.objectdataengine.service;

import com.sjtu.objectdataengine.service.mongodb.RootService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class StartService {
    @Autowired
    public RootService rootService;

    @PostConstruct
    public void findRoot() {
        rootService.find();
        System.out.println("根节点检测完毕");
    }
}
