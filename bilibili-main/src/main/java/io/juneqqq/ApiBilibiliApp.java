package io.juneqqq;

import io.juneqqq.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Optional;

/**
 * @author june
 */
@SpringBootApplication
@EnableAsync
@EnableScheduling
@EnableCaching
@Slf4j
public class ApiBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ApiBilibiliApp.class, args);
        WebSocketService.exposeApplicationContext(app);
        Environment environment = app.getBean(Environment.class);
        log.debug("Swagger 访问链接：http://localhost:" + environment.getProperty("server.port") +
                Optional.ofNullable(environment.getProperty("server.servlet.context-path")).orElse("") + "/swagger-ui.html");
    }
}
