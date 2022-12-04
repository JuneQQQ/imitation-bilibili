package com.juneqqq.service.common;

import com.juneqqq.dao.UserRoleDao;
import com.juneqqq.entity.auth.UserRole;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class UserRoleService {

    @Resource
    private UserRoleDao userRoleDao;

    public List<UserRole> getUserRoleByUserId(Long userId) {
        return userRoleDao.getUserRoleByUserId(userId);
    }

    public void addUserRole(UserRole userRole) {
        userRoleDao.insert(userRole);
    }
}
