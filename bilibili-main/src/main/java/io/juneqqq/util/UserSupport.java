package io.juneqqq.util;

import cn.hutool.core.lang.Pair;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.juneqqq.constant.UserConstant;
import io.juneqqq.core.auth.auth.SystemConfigConstant;
import io.juneqqq.core.exception.BusinessException;
import com.mysql.cj.util.StringUtils;
import io.juneqqq.core.exception.ErrorCodeEnum;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.annotation.Resource;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class UserSupport {
    @Resource
    StringRedisTemplate stringRedisTemplate;

    private static final String ISSUER = "1243134432@qq.com";

    public String getToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();

        return request.getHeader(SystemConfigConstant.HTTP_AUTH_HEADER_NAME);
    }

    @SneakyThrows
    public void deleteToken() {
        String token = getToken();

        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        JWTVerifier verifier = JWT.require(algorithm).build();
        DecodedJWT jwt = verifier.verify(token);

        long userId = Long.parseLong(jwt.getKeyId());
        stringRedisTemplate.delete(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId);
    }

    public Long getCurrentUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert requestAttributes != null;
        HttpServletRequest request = requestAttributes.getRequest();

        String token = request.getHeader(SystemConfigConstant.HTTP_AUTH_HEADER_NAME);

        Pair<Long, String> pair = null;
        pair = verifyToken(token);
        return pair.getKey();
    }

    /**
     * 校验token
     */
    public Pair<Long, String> verifyToken(String token) {
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
            try {
                return new Pair<>(userId, refreshToken(userId));
            } catch (Exception ex) {
                throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
    }


    @SneakyThrows
    public String generateToken(Long userId) {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.HOUR, 240);
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
            log.debug("token 在 redis也过期了");
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        String newAccessToken = generateToken(userId);  // 如果前端不想更新，那就不用每次都新生成再返回
        String newRefreshToken = generateRefreshToken(userId);
        stringRedisTemplate.opsForValue().set(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId + ":",
                newRefreshToken, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return newAccessToken;
    }


}
