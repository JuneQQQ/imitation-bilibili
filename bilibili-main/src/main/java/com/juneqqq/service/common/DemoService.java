package com.juneqqq.service.common;

import com.juneqqq.dao.DemoDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class DemoService {

    @Resource
    private DemoDao demoDao;



    public Long query(Long id){
        return demoDao.query(id);
    }
}
