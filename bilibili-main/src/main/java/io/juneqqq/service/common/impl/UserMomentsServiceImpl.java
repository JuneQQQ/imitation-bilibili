package io.juneqqq.service.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.dao.mapper.UserMomentsMapper;
import io.juneqqq.constant.CacheConstant;
import io.juneqqq.constant.UserMomentsConstant;
import io.juneqqq.dao.entity.UserMoment;
import io.juneqqq.core.exception.CustomException;
import io.juneqqq.service.common.UserMomentService;
import io.juneqqq.util.RocketMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserMomentsServiceImpl implements UserMomentService {

    @Resource
    private UserMomentsMapper userMomentsMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    DefaultMQProducer momentsProducer;

    public void addUserMoments(UserMoment userMoment) {
        Long hasBefore = userMomentsMapper.selectCount(new LambdaQueryWrapper<UserMoment>()
                .eq(UserMoment::getContentId, userMoment.getContentId()));
        if (hasBefore == 0) {
            userMomentsMapper.insert(userMoment);
            userMoment.setCreateTime(null);
            userMoment.setUpdateTime(null);
            Message msg = new Message(UserMomentsConstant.TOPIC_MOMENTS,
                    JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
            // 准备消息给RocketMQ
            try {
                RocketMQUtil.syncSendMsg(momentsProducer, msg);
            } catch (Exception e) {
                throw new CustomException("MQ消息未发送成功，异常信息："+e.getMessage());
            }
        } else {
            throw new CustomException("不允许重复值插入！");
        }

    }

    public List<UserMoment> getUserMoments(Long userId) {
        List<UserMoment> userMoments = userMomentsMapper.selectList(new LambdaQueryWrapper<>(UserMoment.class)
                .eq(UserMoment::getUserId, userId));
        return userMoments;
    }

    public Set<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = CacheConstant.USER_SUBSCRIBED_CACHE_NAME + userId;
        log.debug("key:" + key);
        // 从redis拿取所有我订阅的动态
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null) members = new HashSet<>();
        log.debug(members.toString());
        return members.stream().map(i -> JSONObject.parseObject(i, UserMoment.class)).collect(Collectors.toSet());
    }
}
