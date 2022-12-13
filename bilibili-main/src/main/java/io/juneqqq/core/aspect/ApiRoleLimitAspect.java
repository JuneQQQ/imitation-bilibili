//package io.juneqqq.core.aspect;
//
//
//import io.juneqqq.core.annotation.ApiRoleLimit;
//import io.juneqqq.core.auth.UserRole;
//import io.juneqqq.core.exception.CustomException;
//import io.juneqqq.service.common.UserRoleService;
//import io.juneqqq.util.UserSupport;
//import lombok.extern.slf4j.Slf4j;
//import org.aspectj.lang.JoinPoint;
//import org.aspectj.lang.annotation.*;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.Resource;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Aspect
//@Slf4j
//@Component
//public class ApiRoleLimitAspect {
//
//    @Resource
//    private UserSupport userSupport;
//
//    @Resource
//    private UserRoleService userRoleService;
//
//    @Pointcut("within(io.juneqqq.controller.*)")
//    public void check() {
//    }
//
//    @Before("check()&&@annotation(apiRoleLimit)")
//    public void doBefore(JoinPoint joinPoint, ApiRoleLimit apiRoleLimit) {
//        log.debug("joinPoint:" + joinPoint);
//        log.debug("apiLimitedRole增强Before！！");
//        Long userId = userSupport.getCurrentUserId();
//        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
//        log.debug("用户的角色：" + userRoleList);
//        String[] limitedRoleCodeList = apiRoleLimit.deny();
//        Set<String> denyList = Arrays.stream(limitedRoleCodeList).collect(Collectors.toSet());
//        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
//
//        log.debug(denyList.toString());
//        log.debug(roleCodeSet.toString());
//
//        roleCodeSet.retainAll(denyList);
//
//        if (roleCodeSet.size() > 0) {
//            throw new CustomException("角色权限不足！");
//        }
//    }
//}
