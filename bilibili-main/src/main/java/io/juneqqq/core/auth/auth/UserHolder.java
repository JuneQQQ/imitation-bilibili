package io.juneqqq.core.auth.auth;

import lombok.experimental.UtilityClass;

/**
 * 用户信息 持有类
 *
 * @author xiongxiaoyang
 * @date 2022/5/18
 */
@UtilityClass
public class UserHolder {

    /**
     * 当前线程用户ID
     */
    private static final ThreadLocal<Long> userIdTL = new ThreadLocal<>();


    public void setUserId(Long userId) {
        userIdTL.set(userId);
    }

    public Long getUserId() {
        return userIdTL.get();
    }


    public void clear() {
        userIdTL.remove();
    }

}
