package io.juneqqq.dao.mapper;

import io.juneqqq.core.auth.AuthRole;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuthRoleMapper {

    AuthRole getRoleByCode(String code);
}
