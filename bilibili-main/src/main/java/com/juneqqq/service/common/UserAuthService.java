package com.juneqqq.service.common;

import com.juneqqq.entity.auth.AuthRole;
import com.juneqqq.entity.auth.UserAuthorities;
import com.juneqqq.entity.auth.UserRole;
import com.juneqqq.entity.constant.AuthRoleConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Service
public class UserAuthService {

    @Resource
    private UserRoleService userRoleService;

    @Resource
    private AuthRoleService authRoleService;

    @Resource
    ThreadPoolExecutor threadPoolExecutor;

    public UserAuthorities getUserAuthorities(Long userId){
        UserAuthorities userAuthorities = new UserAuthorities();
        CompletableFuture<Set<Long>> c1 = CompletableFuture.supplyAsync(() -> {
            List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
            // 抽取roleIds
            return userRoleList.stream().map(UserRole::getRoleId).collect(Collectors.toSet());
            // 抽取roleIds
        }, threadPoolExecutor);
        CompletableFuture<Void> c2 = c1.thenAcceptAsync((roleIdSet) -> {
            //  操作权限
            userAuthorities.setRoleElementOperationList(authRoleService.getRoleElementOperationsByRoleIds(roleIdSet));
        });
        CompletableFuture<Void> c3 = c1.thenAcceptAsync((roleIdSet) -> {
            //  菜单权限
            userAuthorities.setRoleMenuList(authRoleService.getAuthRoleMenusByRoleIds(roleIdSet));
        });
        try {
            CompletableFuture.allOf(c1, c2, c3).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return userAuthorities;
    }

    public void addUserDefaultRole(Long id) {
        UserRole userRole = new UserRole();
        AuthRole role = authRoleService.getRoleByCode(AuthRoleConstant.ROLE_LV1);
        userRole.setUserId(id);
        userRole.setRoleId(role.getId());
        userRoleService.addUserRole(userRole);
    }
}
