package io.juneqqq.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@Tag(name = "TestController", description = "这是一个测试模块")
public class TestController {
    @GetMapping("/run")
    public String demo(){
        return "success";
    }
}
