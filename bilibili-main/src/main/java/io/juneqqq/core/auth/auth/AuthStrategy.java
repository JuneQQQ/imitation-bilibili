package io.juneqqq.core.auth.auth;

import io.juneqqq.cache.UserInfoCacheManager;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
import io.juneqqq.util.JwtUtils;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * 策略模式实现用户认证授权功能
 */
public interface AuthStrategy {

    /**
     * 用户认证授权
     *
     * @param token      登录 token
     * @param requestUri 请求的 URI
     * @throws BusinessException 认证失败则抛出业务异常
     */
    void auth(String token, String requestUri) throws BusinessException;

    /**
     * 前台多系统单点登录统一账号认证授权
     *
     * @param jwtUtils             jwt 工具
     * @param userInfoCacheManager 用户缓存管理对象
     * @param token                token 登录 token
     */
    default void authSSO(JwtUtils jwtUtils, UserInfoCacheManager userInfoCacheManager,
                         String token) {
        if (!StringUtils.hasText(token)) {
            // token 为空
            throw new BusinessException(ErrorCodeEnum.USER_LOGIN_EXPIRED);
        }
        Long userId = jwtUtils.parseToken(token, SystemConfigConstant.BILIBILI_FRONT_KEY);
        CacheUserInfoDto userInfo = userInfoCacheManager.getUserInfo(userId);
        if (Objects.isNull(userInfo)) {
            // 用户不存在
            throw new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST);
        }
        // 设置 userId 到当前线程
        UserHolder.setUserId(userId);
    }

}
