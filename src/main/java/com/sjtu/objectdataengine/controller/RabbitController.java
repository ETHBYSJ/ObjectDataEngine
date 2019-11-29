package com.sjtu.objectdataengine.controller;

import com.sjtu.objectdataengine.rabbitMQ.RabbitMQTest.SenderTest;
import com.sjtu.objectdataengine.service.rabbit.RabbitMQService;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/rabbit")
@RestController
public class RabbitController {
    @Autowired
    private RabbitMQService rabbitMQService;
    @GetMapping("add_queue")
    public void addQueue(@RequestParam String queue, @RequestParam String key) {
        rabbitMQService.addQueue(queue, key);
    }
    @GetMapping("send_msg")
    public void sendMsg(@RequestParam String msg, @RequestParam String key) {
        rabbitMQService.sendMessage(msg, key);
    }
    @GetMapping("bind_queue")
    public void bindQueue(@RequestParam String queue, @RequestParam String key) {
        rabbitMQService.addBinding(new Queue(queue), key);
    }
}
