package io.juneqqq.util;//package io.juneqqq.service.util;
//
//import com.auth0.jwt.JWT;
//import com.auth0.jwt.JWTVerifier;
//import com.auth0.jwt.algorithms.Algorithm;
//import com.auth0.jwt.exceptions.TokenExpiredException;
//import com.auth0.jwt.interfaces.DecodedJWT;
//import io.juneqqq.entity.constant.UserConstant;
//import io.juneqqq.entity.exception.CustomException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.Resource;
//import java.util.Calendar;
//import java.util.Date;
//
//@Slf4j
//@Component
//public class TokenSupport {
//    @Resource
//    StringRedisTemplate stringRedisTemplate;
//
//    private  final String ISSUER = "1243134432@qq.com";
//
//    public  String generateToken(Long userId) throws Exception {
//        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.HOUR, 240);  // TODO 测试环境有效期为240小时
//        return JWT.create().withKeyId(String.valueOf(userId))
//                .withIssuer(ISSUER)
//                .withExpiresAt(calendar.getTime())
//                .sign(algorithm);
//    }
//
//    public  String generateRefreshToken(Long userId) throws Exception {
//        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(new Date());
//        calendar.add(Calendar.SECOND, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS);
//        return JWT.create().withKeyId(String.valueOf(userId))
//                .withIssuer(ISSUER)
//                .withExpiresAt(calendar.getTime())
//                .sign(algorithm);
//    }
//
//    public  Long verifyToken(String token) {
//        try {
//            Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
//            JWTVerifier verifier = JWT.require(algorithm).build();
//            DecodedJWT jwt = verifier.verify(token);
//            String userId = jwt.getKeyId();
//            return Long.valueOf(userId);
//        } catch (TokenExpiredException e) {
//            // JWT accessToken 校验已过期，接下来校验 refreshToken是否过期
//            try {
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//
//            throw new CustomException("555", "token过期！");
//        } catch (Exception e) {
//            throw new CustomException("非法用户token！");
//        }
//    }
//
//
//}
