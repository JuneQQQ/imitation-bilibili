package com.juneqqq.util;

import cn.hutool.core.lang.Pair;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.juneqqq.entity.constant.UserConstant;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.util.RSAUtil;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserSupport {

    @Resource
    StringRedisTemplate stringRedisTemplate;

    private final String ISSUER = "1243134432@qq.com";

    public String getToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        return request.getHeader("token");
    }

    public void deleteToken() {
        String token = getToken();

        Algorithm algorithm = null;
        try {
            algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);

        long userId = Long.parseLong(jwt.getKeyId());
        stringRedisTemplate.delete(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId);

    }

    public Long getCurrentUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = requestAttributes.getRequest();

        String token = request.getHeader("token");

        Pair<Long, String> pair = null;
        try {
            pair = verifyToken(token);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        return pair.getKey();
    }

    /**
     * 校验token
     */
    public Pair<Long, String> verifyToken(String token) throws Exception {
        Long userId = null;
        try {
            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
            JWTVerifier verifier = JWT.require(algorithm).build();
            DecodedJWT jwt = verifier.verify(token);
            userId = Long.valueOf(jwt.getKeyId());
            return new Pair<>(userId, null);
        } catch (TokenExpiredException e) {
            log.debug("用户accessToken过期，开始校验refreshToken，userId：" + userId);
            // JWT accessToken 校验已过期，接下来校验 refreshToken是否过期
            return new Pair<>(userId, refreshToken(userId));
        } catch (Exception e) {
            throw new CustomException("非法用户token！");
        }
    }


    public String generateToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 240);  // TODO 测试环境有效期为240小时
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }

    public String generateRefreshToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.SECOND, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS);
        return JWT.create().withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(calendar.getTime())
                .sign(algorithm);
    }

    /**
     * 刷新redis的refreshToken
     *
     * @return new accessToken
     */
    public String refreshToken(Long userId) throws Exception {
        String oldToken = stringRedisTemplate.opsForValue().get(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId);
        if (StringUtils.isNullOrEmpty(oldToken)) {
            throw new CustomException(555, "token过期！无法刷新！");
        }
        String newAccessToken = generateToken(userId);  // 如果前端不想更新，那就不用每次都新生成再返回
        String newRefreshToken = generateRefreshToken(userId);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId + ":",
                newRefreshToken, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return newAccessToken;
    }


}
