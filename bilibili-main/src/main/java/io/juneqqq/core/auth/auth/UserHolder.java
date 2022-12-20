package io.juneqqq.core.auth.auth;

import cn.hutool.extra.spring.SpringUtil;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.util.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户信息 持有类
 */
public class UserHolder {
    /**
     * 当前线程用户ID
     */
    private static final ThreadLocal<Long> userIdTL = new ThreadLocal<>();


    public static void setUserId(Long userId) {
        userIdTL.set(userId);
    }

    public static Long getUserId() {
        Long id = userIdTL.get();
        if (id == null) {
            // 尝试从header Authorization 中获取id
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            assert requestAttributes != null;
            HttpServletRequest request = requestAttributes.getRequest();

            String token = request.getHeader(SystemConfigConstant.HTTP_AUTH_HEADER_NAME);

            JwtUtils jwtUtils = SpringUtil.getBean(JwtUtils.class);
            if(token==null) throw new BusinessException(ErrorCodeEnum.NO_TOKEN);
            id = jwtUtils.parseToken(token, SystemConfigConstant.BILIBILI_FRONT_KEY);
        }
        return id;
    }

    public static Long getUserId(String token){
        JwtUtils jwtUtils = SpringUtil.getBean(JwtUtils.class);
        return jwtUtils.parseToken(token, SystemConfigConstant.BILIBILI_FRONT_KEY);
    }

    public static void clear() {
        userIdTL.remove();
    }

}
