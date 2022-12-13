package io.juneqqq;


import io.juneqqq.cache.UserInfoCacheManager;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
import io.juneqqq.service.common.UserService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ControllerTest {
    @Resource
    private UserService userService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserInfoCacheManager userInfoCacheManager;
    @Test
    void loadTest() {
        CacheUserInfoDto userInfo = userInfoCacheManager.getUserInfo(1L);
//        userInfoCacheManager.evictUserInfoCacheById(1L);
        userInfoCacheManager.evictUserInfoCacheAll();
        System.out.println(1);
    }

}
