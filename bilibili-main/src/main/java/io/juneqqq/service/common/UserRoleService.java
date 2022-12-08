package io.juneqqq.service.common;

import io.juneqqq.core.auth.UserRole;

import java.util.List;

public interface UserRoleService {
    List<UserRole> getUserRoleByUserId(Long userId);
    void addUserRole(UserRole userRole);
}
