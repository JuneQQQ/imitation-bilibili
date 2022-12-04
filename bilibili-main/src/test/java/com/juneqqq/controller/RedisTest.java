package com.juneqqq.controller;

import com.alibaba.fastjson.JSONObject;
import com.juneqqq.entity.dao.UserMoment;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RedisTest {
    @Resource
    StringRedisTemplate stringObjectRedisTemplate;

    @Test
    void testInsert() {
        Set<Integer> integers = Set.of(1, 2, 5, 3, 4);
    }

    @Test
    void testMultiSet() {
        List<String> keys = List.of("subscribed-32498", "subscribed-36585", "subscribed-64247");
        List<List<UserMoment>> collect = Objects.requireNonNull(stringObjectRedisTemplate.opsForValue().
                        multiGet(keys)).stream().
                map(r -> JSONObject.parseArray(r, UserMoment.class)).
                collect(Collectors.toList());
        System.out.println(keys);
        System.out.println(collect);
    }

    @Test
    void testMultiGet() {
        List<String> keys = List.of("subscribed:32498", "subscribed:36585", "subscribed:64247");
        java.util.List<String> strings = stringObjectRedisTemplate.opsForValue().multiGet(keys);
        System.out.println(strings);
    }

    @Test
    void Serialize() {
        Student student = new Student();
        student.setAge(11);
        student.setName("nihao");
        stringObjectRedisTemplate.opsForValue().set("{test}k1",JSONObject.toJSONString(student));
        stringObjectRedisTemplate.opsForValue().set("{test}k2","nihao");
    }

    @Data
    static class Student implements Serializable {
        public static final long SerializeId = 1L;
        private int age;
        private String name;

        @Override
        public String toString() {
            return "Student{" +
                    "age=" + age +
                    ", name='" + name + '\'' +
                    '}';
        }
    }
}
