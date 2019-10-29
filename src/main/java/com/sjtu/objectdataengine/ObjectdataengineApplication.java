package com.sjtu.objectdataengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ObjectdataengineApplication {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getId());
        SpringApplication.run(ObjectdataengineApplication.class, args);
        /*HashSet<String> attr = new HashSet<String>();
        ObjectTemplate objectTemplate = new ObjectTemplate("1", "123", attr);
        objectTemplate.setCreateTime(new Date());*/
    }


}
