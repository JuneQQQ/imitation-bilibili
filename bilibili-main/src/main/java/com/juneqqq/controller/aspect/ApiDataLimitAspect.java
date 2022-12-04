package com.juneqqq.controller.aspect;


import com.juneqqq.entity.dao.UserMoment;
import com.juneqqq.entity.annotation.ApiDataLimit;
import com.juneqqq.entity.auth.UserRole;
import com.juneqqq.entity.constant.AuthRoleConstant;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.service.common.UserRoleService;

import com.juneqqq.util.UserSupport;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Order(1)
@Component
@Aspect
@Slf4j
public class ApiDataLimitAspect {

    @Resource
    private UserSupport userSupport;

    @Resource
    private UserRoleService userRoleService;

    @Pointcut("within(com.juneqqq.controller.*)")
    public void check() {
    }

    @Before("check()&&@annotation(apiDataLimit)")
    public void doBefore(JoinPoint joinPoint, ApiDataLimit apiDataLimit) {
        Long userId = userSupport.getCurrentUserId();
        List<UserRole> userRoleList = userRoleService.getUserRoleByUserId(userId);
        Set<String> roleCodeSet = userRoleList.stream().map(UserRole::getRoleCode).collect(Collectors.toSet());
        Object[] args = joinPoint.getArgs();

        log.debug("【Data校验】校验数据：" + roleCodeSet);
        for (Object arg : args) {
            if (arg instanceof UserMoment userMoment) {
                String type = userMoment.getType();
                if (roleCodeSet.contains(AuthRoleConstant.ROLE_LV1) && !"3".equals(type)) {
                    throw new CustomException("Level1 仅允许【发表普通用户动态】");
                }
            }
        }
    }
}
