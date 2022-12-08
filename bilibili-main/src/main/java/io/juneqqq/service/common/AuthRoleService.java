package io.juneqqq.service.common;

import io.juneqqq.core.auth.AuthRole;
import io.juneqqq.core.auth.AuthRoleElementOperation;
import io.juneqqq.core.auth.AuthRoleMenu;

import java.util.List;
import java.util.Set;

public interface AuthRoleService {
    List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet);
    List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet);
    AuthRole getRoleByCode(String code);
}
