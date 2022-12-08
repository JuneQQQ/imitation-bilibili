package io.juneqqq.service.common;

import io.juneqqq.core.auth.UserAuthorities;

public interface UserAuthService {
    UserAuthorities getUserAuthorities(Long userId);
    void addUserDefaultRole(Long id);
}
