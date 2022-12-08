//package io.juneqqq.controller;
//
//import cn.hutool.core.lang.Pair;
//import cn.hutool.crypto.SecureUtil;
//import cn.hutool.json.JSONUtil;
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.fastjson.serializer.SerializerFeature;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//
//import io.juneqqq.dao.mapper.UserMapper;
//import io.juneqqq.dao.mapper.UserFollowingMapper;
//import io.juneqqq.dao.mapper.UserInfoMapper;
//import io.juneqqq.dao.entity.User;
//import io.juneqqq.dao.entity.UserInfo;
//import io.juneqqq.service.common.impl.DemoService;
//import io.juneqqq.service.common.impl.UserService;
//
//import io.juneqqq.util.MD5Util;
//import io.juneqqq.util.RSAUtil;
//import io.juneqqq.util.UserSupport;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//
//import jakarta.annotation.Resource;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//class CommonTest {
//
//
//
//    @Resource
//    StringRedisTemplate stringRedisTemplate;
//
//
//    @Test
//    void testAnnotation() {
//        Integer coinAmount = userService.getCoinAmount(1L);
//        System.out.println(coinAmount);
//    }
//
//    @Test
//    void testRedis() {
//        stringRedisTemplate.opsForValue().set("test001", "2");
//        Set<String> keys = stringRedisTemplate.keys("*");
//        System.out.println(keys);
//    }
//
//
//
//    @Resource
//    UserSupport userSupport;
//
//    @Resource
//    UserFollowingMapper userFollowingMapper;
//
//
//
//    void getTestToken() throws Exception {
//        User user = new User();
//        user.setEmail(null);
//        user.setPhone("7183690836");
//        user.setPassword(RSAUtil.encrypt("111111"));
//
//        String token = userService.login(user);
//        System.out.println(token);
//
//        Pair<Long, String> p = userSupport.verifyToken(token);
//        System.out.println('\n'+p.getKey());
//
//        // 一号用户关注了二号用户
//        // 2  8382815487  eyJraWQiOiIyIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiIxMjQzMTM0NDMyQHFxLmNvbSIsImV4cCI6MTY3MDI5MDc3MX0.VLRfyRC_mZINH3vJ5sFVAOFiya3uYwFlb6Bm8sRC1A4lDDdAzZUgmNjVzSBbwRNfpvML1RUUR8kjHrg8r5bzH5CT81Xvwg6bf5g-mEIRV_7swvx9ZoaUgyr3EqG4CqErAEd4nK1CEe8MvZ4wk0WKp_o_p_aR6D3kzrdItdpazOA
//        // 1  7183690836  eyJraWQiOiIxIiwidHlwIjoiSldUIiwiYWxnIjoiUlMyNTYifQ.eyJpc3MiOiIxMjQzMTM0NDMyQHFxLmNvbSIsImV4cCI6MTY3MDI5MDc5NH0.PyxsqMPuvV5uwnA2QAmAeuJBDiPiLK5INBnU8LFxso54d1DVzmdKNZ4s7PBI7YXMMNMG1IX17WfhOOIilPxO2CCxyF3U9U3oIAjK-Wsuzz4_WC-BNt5vbvbrj8X9rm9vwny-RTOLd1OKn_lgFPPEnV3tHSyRUrfHgAiVoP4iKcE
//    }
//
//
//    @Test
//    void addTestUser() throws Exception {
////        User user = new User();
////        user.setEmail("111111");
////        user.setPhone("111111");
////        user.setSalt("111111");
////        String sign = MD5Util.sign("111111", "111111", "UTF-8");
////        user.setPassword(sign);
////        userDao.insert(user);
//////        userService.addUser(user);
////        log.info("登录名：" + "111111");
////        log.info("密码：" + "111111");
//    }
//
//
//    @Test
//    void testJWT() throws Exception {
//
////        String token = JWTUtil.createToken(
////                new HashMap<String, Object>() {{
////                    put("kid", "nihao");
////                }},
////                "this is akey".getBytes()
////        );
//        String token = "eyJraWQiOiI1OTkxOSIsInR5cCI6IkpXVCIsImFsZyI6IlJTMjU2In0.eyJpc3MiOiIxMjQzMTM0NDMyQHFxLmNvbSIsImV4cCI6MTY2OTIwOTY1OH0.fthDS5YfpccHlWkgG931O2vKIlcb9v180h73ulXyx8HRjpW1cpLUX72cPwgfozNMSeWbobPVDx3waOoQ5WxPTouyuQ_W55JskWBGWil5IQ_Hfy_7S3PBoTnA9VHAH6hlTWcZLGxnaFJnyehEbCh6JE16Y1laVVRsCk2Q6kB6sks";
//        Pair<Long, String> p = userSupport.verifyToken(token);
//        System.out.println(p.getKey());
//    }
//
//    @Test
//    void testMybatisPlus() {
////        demoService.testInsert();
////        User user = new User();
////        user.setPassword("nihao");
////        user.setSalt("nihao");
////        user.setEmail("nihao");
////        user.setEmail("nihao");
////        userService.addUserTest(user);
//
//    }
//
//    @Test
//    void test() {
//        String s = SecureUtil.pbkdf2("nihao".toCharArray(), "salt".getBytes(StandardCharsets.UTF_8));
//        String s1 = SecureUtil.pbkdf2("nihao".toCharArray(), "sadt".getBytes(StandardCharsets.UTF_8));
//        System.out.println(s);
//        System.out.println(s1);
//    }
//
//    @Test
//    void test1() {
//        String md5Password = MD5Util.sign("rawPassword", "salt", "UTF-8");
//        String md5Password1 = MD5Util.sign("rawPassword", "alt", "UTF-8");
//        System.out.println(md5Password);
//        System.out.println(md5Password1);
//    }
//
//    @Test
//    void testJSON() {
//        Student student = new Student(1, "222", null);
//        List<Student> l = new ArrayList<>();
//        l.add(student);
//        l.add(student);
//        long l1 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            String s1 = JSONObject.toJSONString(l, SerializerFeature.DisableCircularReferenceDetect);
//        }
//        System.out.println("fastjson:" + (System.currentTimeMillis() - l1));
//        long l2 = System.currentTimeMillis();
//        for (int i = 0; i < 100000; i++) {
//            String s = JSONUtil.toJsonStr(l);
//        }
//        System.out.println("hutool:" + (System.currentTimeMillis() - l2));
//
//    }
//
//    @Test
//    void testTime() {
//        String startTime = "2021-12-22 12:12:12";
//        String endTime = "2022-12-22 12:12:12";
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime startDate = LocalDateTime.parse(startTime, dtf);
//        LocalDateTime endDate = LocalDateTime.parse(endTime, dtf);
//        System.out.println(startDate);
//        System.out.println(endDate);
//
//    }
//
//
//    @Data
//    @AllArgsConstructor
//    static class Student {
//        final String a = "a";
//
//
//        int age;
//        String name;
//        Student s;
//    }
//}
//
//
