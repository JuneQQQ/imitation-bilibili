package io.juneqqq.service.common.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dao.mapper.UserMomentsMapper;
import io.juneqqq.cache.CacheConstant;
import io.juneqqq.constant.RocketMQConstant;
import io.juneqqq.pojo.dao.entity.UserMoment;
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
            Message msg = new Message(RocketMQConstant.TOPIC_MOMENTS,
                    JSONObject.toJSONString(userMoment).getBytes(StandardCharsets.UTF_8));
            // 准备消息给RocketMQ
            try {
                RocketMQUtil.syncSendMsg(momentsProducer, msg);
            } catch (Exception e) {
                log.error("MQ消息未发送成功，异常信息：{}", e.getMessage());
                throw new BusinessException(ErrorCodeEnum.MQ_MESSAGE_SEND_FAILED);
            }
        } else {
            throw new BusinessException(ErrorCodeEnum.USER_MOMENT_ID_HAS_EXISTED);
        }

    }

    public List<UserMoment> getUserMoments(Long userId) {
        return userMomentsMapper.selectList(new LambdaQueryWrapper<>(UserMoment.class)
                .eq(UserMoment::getUserId, userId));
    }

    public Set<UserMoment> getUserSubscribedMoments(Long userId) {
        String key = CacheConstant.USER_SUBSCRIBED + userId;
        log.debug("key:" + key);
        // 从redis拿取所有我订阅的动态
        Set<String> members = stringRedisTemplate.opsForSet().members(key);
        if (members == null) members = new HashSet<>();
        log.debug(members.toString());
        return members.stream().map(i -> JSONObject.parseObject(i, UserMoment.class)).collect(Collectors.toSet());
    }
}
