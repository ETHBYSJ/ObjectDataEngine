package com.sjtu.objectdataengine.service;

import com.sjtu.objectdataengine.dao.MongoRootDAO;
import com.sjtu.objectdataengine.dao.RedisRootDAO;
import com.sjtu.objectdataengine.model.RootMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;

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
