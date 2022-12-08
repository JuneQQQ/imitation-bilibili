package io.juneqqq.service.common.impl;

import io.juneqqq.dao.mapper.AuthRoleMapper;
import io.juneqqq.core.auth.AuthRole;
import io.juneqqq.core.auth.AuthRoleElementOperation;
import io.juneqqq.core.auth.AuthRoleMenu;
import io.juneqqq.service.common.AuthRoleElementOperationService;
import io.juneqqq.service.common.AuthRoleMenuService;
import io.juneqqq.service.common.AuthRoleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleServiceImpl implements AuthRoleService {

    @Resource
    private AuthRoleMapper authRoleMapper;

    @Resource
    private AuthRoleElementOperationService authRoleElementOperationService;

    @Resource
    private AuthRoleMenuService authRoleMenuService;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationService.getRoleElementOperationsByRoleIds(roleIdSet);
    }

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuService.getAuthRoleMenusByRoleIds(roleIdSet);
    }

    public AuthRole getRoleByCode(String code) {
        return authRoleMapper.getRoleByCode(code);
    }
}
