package io.juneqqq.service.common.impl;

import io.juneqqq.pojo.dao.mapper.AuthRoleMenuMapper;
import io.juneqqq.core.auth.AuthRoleMenu;
import io.juneqqq.service.common.AuthRoleMenuService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleMenuServiceImpl implements AuthRoleMenuService {

    @Resource
    private AuthRoleMenuMapper authRoleMenuMapper;

    public List<AuthRoleMenu> getAuthRoleMenusByRoleIds(Set<Long> roleIdSet) {
        return authRoleMenuMapper.getAuthRoleMenusByRoleIds(roleIdSet);
    }
}
