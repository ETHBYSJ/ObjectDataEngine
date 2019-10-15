package com.sjtu.objectdataengine;

import com.sjtu.objectdataengine.model.ObjectTemplate;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.HashSet;

@SpringBootApplication
public class ObjectDataEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObjectDataEngineApplication.class, args);
        /*HashSet<String> attr = new HashSet<String>();
        ObjectTemplate objectTemplate = new ObjectTemplate("1", "123", attr);
        objectTemplate.setCreateTime(new Date());*/
    }


}
