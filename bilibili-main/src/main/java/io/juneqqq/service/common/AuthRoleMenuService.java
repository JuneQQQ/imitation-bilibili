package io.juneqqq.service.common;

import io.juneqqq.core.auth.AuthRoleMenu;

import java.util.List;
import java.util.Set;

public interface AuthRoleMenuService {
    List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet);
}
