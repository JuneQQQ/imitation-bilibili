package io.juneqqq.service.common.impl;

import io.juneqqq.dao.mapper.DemoMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;

@Service
public class DemoService {

    @Resource
    private DemoMapper demoMapper;

    public Long query(Long id){
        return demoMapper.query(id);
    }
}
