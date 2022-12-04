package com.juneqqq.service.common;

import com.juneqqq.dao.AuthRoleElementOperationDao;
import com.juneqqq.entity.auth.AuthRoleElementOperation;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleElementOperationService {

    @Resource
    private AuthRoleElementOperationDao authRoleElementOperationDao;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationDao.getRoleElementOperationsByRoleIds(roleIdSet);
    }
}
