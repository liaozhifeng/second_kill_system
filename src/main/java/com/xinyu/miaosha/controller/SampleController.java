package com.xinyu.miaosha.controller;

import com.xinyu.miaosha.rabbitmq.MQSender;
import com.xinyu.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/demo")
public class SampleController {

    @Autowired
    MQSender sender;

    @RequestMapping("/hello")
    public String thymeleaf(Model model) {
        model.addAttribute("name", "xinyu");
        return "hello";
    }

    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> rabbitMq() {
        sender.send("hello rabbitMq");
        return Result.success("hello");
    }

    @RequestMapping("/topic")
    @ResponseBody
    public Result<String> topicMq() {
        sender.sendTopic("topic rabbitMq");
        return Result.success("hello");
    }

    @RequestMapping("/fanout")
    @ResponseBody
    public Result<String> fanoutMq() {
        sender.sendFanout("fanout rabbitMq");
        return Result.success("fanout");
    }

    @RequestMapping("/headers")
    @ResponseBody
    public Result<String> headersMq() {
        sender.sendHeaders("headers rabbitMq");
        return Result.success("headers");
    }
}
