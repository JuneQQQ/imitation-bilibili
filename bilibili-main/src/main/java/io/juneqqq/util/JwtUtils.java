package io.juneqqq.util;

import cn.hutool.core.date.DateUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.juneqqq.cache.CacheConstant;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
/**
 * JWT 组件
 */
@ConditionalOnProperty("system.jwt.secret")
@Component("jwtUtils")
@Slf4j
public class JwtUtils {

    /**
     * 注入JWT加密密钥
     */
    @Value("${system.jwt.secret}")
    private String secret;
    @Value("${system.jwt.expire-seconds}")
    private Integer expire; // 前台token到期时间

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 定义系统标识头常量
     */
    private static final String HEADER_SYSTEM_KEY = "SystemKeyHeader";

    /**
     * 根据用户ID生成JWT
     *
     * @param uid       用户ID
     * @param systemKey 系统标识
     * @return JWT
     */
    public String generateToken(Long uid, String systemKey) {
        return Jwts.builder()
                .setHeaderParam(HEADER_SYSTEM_KEY, systemKey)
                .setSubject(uid.toString())
                .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                .setExpiration(DateUtil.offsetSecond(new Date(), expire))
                .compact();
    }

    /**
     * 解析JWT返回用户ID
     *
     * @param token     JWT
     * @param systemKey 系统标识
     * @return 用户ID
     */
    public Long parseToken(String token, String systemKey) {
        Jws<Claims> claimsJws;
        try {
            claimsJws = Jwts.parserBuilder()
                    .setSigningKey(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)))
                    .build()
                    .parseClaimsJws(token);
            // OK, we can trust this JWT
            // 判断该 JWT 是否属于指定系统
            if (Objects.equals(claimsJws.getHeader().get(HEADER_SYSTEM_KEY), systemKey)) {
                return Long.parseLong(claimsJws.getBody().getSubject());
            }
        } catch (ExpiredJwtException e) {
            // 已过期，查询Redis
            // 查询并设置ttl
            String userId = Optional.ofNullable(stringRedisTemplate.opsForValue().getAndExpire(CacheConstant.USER_REFRESH_TOKEN + ":" + token, (long) 60 * 5, TimeUnit.SECONDS))
                    .orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED));
            return Long.valueOf(userId);
        } catch (JwtException e) {
            log.warn("JWT解析失败:{}，错误信息：{}", token, e.getMessage());
        }
        // what happened?
        throw new BusinessException(ErrorCodeEnum.SYSTEM_ERROR);
    }
}
