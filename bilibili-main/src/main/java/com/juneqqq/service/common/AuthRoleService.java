package com.juneqqq.service.common;

import com.juneqqq.dao.AuthRoleDao;
import com.juneqqq.entity.auth.AuthRole;
import com.juneqqq.entity.auth.AuthRoleElementOperation;
import com.juneqqq.entity.auth.AuthRoleMenu;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleService {

    @Resource
    private AuthRoleDao authRoleDao;

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
        return authRoleDao.getRoleByCode(code);
    }
}
