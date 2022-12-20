package io.juneqqq.core.auth.auth;

import io.juneqqq.cache.UserInfoCacheManager;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.util.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 前台门户系统 认证授权策略
 */
@Component
public class FrontAuthStrategy implements AuthStrategy {

    @Resource
    JwtUtils jwtUtils;
    @Resource
    UserInfoCacheManager userInfoCacheManager;
    /**
     * 不需要 token 认证的接口，也可以在 InterceptorConfig 配置
     */
    private static final List<String> EXCLUDE_PREFIX_URI = List.of(
            ApiRouterConstant.API_FRONT_SEARCH_URL_PREFIX, // 放行 search 接口
            // 测试方便，视频相关接口全部放开
            ApiRouterConstant.API_FRONT_RESOURCE_URL_PREFIX,
            ApiRouterConstant.API_FRONT_VIDEO_URL_PREFIX
    );

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        for (String prefix : EXCLUDE_PREFIX_URI) {
            if (requestUri.startsWith(prefix)) return;
        }
        // 统一账号认证
        authSSO(jwtUtils, userInfoCacheManager, token);
        // 没有异常则认证通过
    }
}