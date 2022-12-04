package com.juneqqq;

import com.juneqqq.service.websocket.WebSocketService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
//@EnableFeignClients(basePackages = "com.juneqqq.service.feign")
public class ApiBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ApiBilibiliApp.class, args);
        WebSocketService.exposeApplicationContext(app);
    }
}
