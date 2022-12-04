package com.juneqqq.service.common;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.juneqqq.dao.UserMomentsDao;
import com.juneqqq.entity.constant.RedisPrefix;
import com.juneqqq.entity.constant.UserMomentsConstant;
import com.juneqqq.entity.dao.UserMoment;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.util.RocketMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserMomentsService {

    @Resource
    private UserMomentsDao userMomentsDao;

    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


    @Resource
    DefaultMQProducer momentsProducer;

    public void addUserMoments(UserMoment userMoment) throws Exception {
        Long hasBefore = userMomentsDao.selectCount(new LambdaQueryWrapper<UserMoment>()
                .eq(UserMoment::getContentId, userMoment.getContentId()));
        if (hasBefore == 0) {
            userMomentsDao.insert(userMoment);
            userMoment.setCreateTime(null);
            userMoment.setUpdateTime(null);
            Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS,
                    JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
            // 准备消息给RocketMQ
            RocketMQUtil.syncSendMsg(momentsProducer, msg);
        } else {
            throw new CustomException("不允许重复值插入！");
        }

    }

    public List<UserMoment> getUserMoments(Long userId) {
        List<UserMoment> userMoments = userMomentsDao.selectList(new LambdaQueryWrapper<>(UserMoment.class)
                .eq(UserMoment::getUserId, userId));
        return userMoments;
    }

    public Set<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = RedisPrefix.USER_SUBSCRIBED + userId;
        log.debug("key:" + key);
        // 从redis拿取所有我订阅的动态
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null) members = new HashSet<>();
        log.debug(members.toString());
        return members.stream().map(i -> JSONObject.parseObject(i, UserMoment.class)).collect(Collectors.toSet());
    }
}
