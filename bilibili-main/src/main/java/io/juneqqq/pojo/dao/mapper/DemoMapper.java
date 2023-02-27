package io.juneqqq.pojo.dao.mapper;


import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DemoMapper {

    Long query(Long id);
}
