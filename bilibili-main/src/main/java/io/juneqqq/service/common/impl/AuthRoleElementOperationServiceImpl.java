package io.juneqqq.service.common.impl;

import io.juneqqq.dao.mapper.AuthRoleElementOperationMapper;
import io.juneqqq.core.auth.AuthRoleElementOperation;
import io.juneqqq.service.common.AuthRoleElementOperationService;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Set;

@Service
public class AuthRoleElementOperationServiceImpl implements AuthRoleElementOperationService {

    @Resource
    private AuthRoleElementOperationMapper authRoleElementOperationMapper;

    public List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet) {
        return authRoleElementOperationMapper.getRoleElementOperationsByRoleIds(roleIdSet);
    }
}
