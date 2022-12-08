package io.juneqqq.core.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD,ElementType.TYPE})
@Documented
@Component
public @interface ApiRoleLimit {

    String[] deny() default {};
}
