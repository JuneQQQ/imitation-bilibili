package io.juneqqq.core.auth.auth;

import io.juneqqq.core.exception.BusinessException;
import org.springframework.stereotype.Component;

/**
 * 平台后台管理系统 认证授权策略
 */
@Component
public class AdminAuthStrategy implements AuthStrategy {

    @Override
    public void auth(String token, String requestUri) throws BusinessException {
        // TODO 平台后台 token 校验
    }
    
}