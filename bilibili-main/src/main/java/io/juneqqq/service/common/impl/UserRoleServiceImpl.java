package io.juneqqq.service.common.impl;

import io.juneqqq.pojo.dao.mapper.UserRoleMapper;
import io.juneqqq.core.auth.UserRole;
import io.juneqqq.service.common.UserRoleService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Resource
    private UserRoleMapper userRoleMapper;

    public List<UserRole> getUserRoleByUserId(Long userId) {
        return userRoleMapper.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRoleMapper.insert(userRole);
    }
}
