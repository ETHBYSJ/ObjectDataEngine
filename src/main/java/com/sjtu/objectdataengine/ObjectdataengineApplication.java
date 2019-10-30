package com.sjtu.objectdataengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ObjectdataengineApplication {

    public static void main(String[] args) {
        System.out.println(Thread.currentThread().getId());
        SpringApplication.run(ObjectdataengineApplication.class, args);
    }

}
