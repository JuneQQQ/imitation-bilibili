package com.juneqqq.service.common;

import com.juneqqq.dao.VideoCommentDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class VideoCommentService {
    @Resource
    private VideoCommentDao videoCommentDao;
}
