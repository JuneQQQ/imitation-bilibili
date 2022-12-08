package io.juneqqq.service.common;

import io.juneqqq.core.auth.AuthRoleElementOperation;

import java.util.List;
import java.util.Set;

public interface AuthRoleElementOperationService {
    List<AuthRoleElementOperation> getRoleElementOperationsByRoleIds(Set<Long> roleIdSet);
}
