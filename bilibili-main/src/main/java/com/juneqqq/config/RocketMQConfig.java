package com.juneqqq.config;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.juneqqq.entity.constant.RedisPrefix;
import com.juneqqq.entity.constant.UserMomentsConstant;
import com.juneqqq.entity.dao.UserFollowing;
import com.juneqqq.entity.dao.UserMoment;
import com.juneqqq.service.common.UserFollowingService;
import com.juneqqq.service.websocket.WebSocketService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddr;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private UserFollowingService userFollowingService;


    @Bean
    public DefaultMQProducer testProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_TEST);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    @Bean
    public DefaultMQPushConsumer testConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_TEST);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(UserMomentsConstant.TOPIC_TEST, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
//                if (messages.size() != 0) {
//                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//                }
                log.info(UserMomentsConstant.GROUP_TEST + "收到消息：" + messages);
                for (MessageExt message : messages) {
                    log.info(Arrays.toString(message.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }


    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    /**
     * 给所有粉丝的订阅set添加一条记录
     */
    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(UserMomentsConstant.TOPIC_MOMENTS, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                MessageExt msg = messages.get(0);
                Optional.ofNullable(msg).ifPresentOrElse(m -> {
                    log.debug("收到消息【momentsConsumer】：" + new String(msg.getBody()));
                    String bodyStr = new String(msg.getBody());
                    UserMoment userMoment = JSONObject.toJavaObject(JSONObject.parseObject(bodyStr), UserMoment.class);
                    Long userId = userMoment.getUserId();
                    List<UserFollowing> fanList = userFollowingService.getUserFans(userId);
                    // 自己也是自己的粉丝
                    if (CollectionUtil.isEmpty(fanList)) fanList = new ArrayList<>();
                    fanList.add(UserFollowing.builder().userId(userId).build());

                    for (UserFollowing fan : fanList) {
                        // 给每个粉丝动态列表添加这样一条记录
                        String key = RedisPrefix.USER_SUBSCRIBED + fan.getUserId();
                        stringRedisTemplate.opsForSet().add(key, bodyStr);
                    }
                }, () -> log.warn("收到了一条空消息？messages:" + messages));

                log.debug("\n消息处理完毕");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception {
        // 实例化消息生产者Producer
        DefaultMQProducer producer = new DefaultMQProducer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        producer.setNamesrvAddr(nameServerAddr);
        // 启动Producer实例
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        // 实例化消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(UserMomentsConstant.GROUP_DANMUS);
        // 设置NameServer的地址
        consumer.setNamesrvAddr(nameServerAddr);
        // 订阅一个或者多个Topic，以及Tag来过滤需要消费的消息
        consumer.subscribe(UserMomentsConstant.TOPIC_DANMUS, "*");
        // 注册回调实现类来处理从broker拉取回来的消息
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                MessageExt msg = messages.get(0);
                log.debug("danmu消费者收到消息{}",msg);
                Optional.ofNullable(msg).ifPresentOrElse(m -> {
                    byte[] msgByte = msg.getBody();
                    String bodyStr = new String(msgByte);
                    JSONObject jsonObject = JSONObject.parseObject(bodyStr);
                    String sessionId = jsonObject.getString("sessionId");
                    String message = jsonObject.getString("message");
                    WebSocketService webSocketService = WebSocketService.ONLINE_SESSION_MAP.get(sessionId);
                    if (webSocketService.getSession().isOpen()) {
                        try {
                            webSocketService.sendMessage(message);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, () -> log.warn("收到了一条空消息？messages:" + messages));

                log.debug("danmu消息消费完毕~");
                // 标记该消息已经被成功消费
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // 启动消费者实例
        consumer.start();
        return consumer;
    }
}