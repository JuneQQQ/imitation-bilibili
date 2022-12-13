package io.juneqqq.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "system.xss")
public record XssProperties(Boolean enabled, List<String> excludes) {

}