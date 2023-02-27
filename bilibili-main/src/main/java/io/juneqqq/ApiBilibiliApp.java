package io.juneqqq;

import com.alicp.jetcache.anno.config.EnableMethodCache;
import io.juneqqq.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
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
@EnableMethodCache(basePackages = "io.juneqqq")
public class ApiBilibiliApp {
    public static void main(String[] args) {
        ApplicationContext app = SpringApplication.run(ApiBilibiliApp.class, args);
        WebSocketService.exposeApplicationContext(app);
        Environment environment = app.getBean(Environment.class);
        String port = environment.getProperty("server.port") == null ? "8080" : environment.getProperty("server.port");
        log.info("Swagger 访问链接：http://localhost:" + port +
                Optional.ofNullable(environment.getProperty("server.servlet.context-path")).orElse("") + "/swagger-ui.html");
        log.info("SpringBoot启动地址：http://127.0.0.1:" + port);
    }

    // Tomcat8以上不允许出现特殊字符，这里做兼容性处理
    @Bean
    public ConfigurableServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers(connector -> connector.setProperty("relaxedQueryChars", "\"<>[\\]^`{|}"));
        return factory;
    }
}
