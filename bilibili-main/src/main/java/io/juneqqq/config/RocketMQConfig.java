package io.juneqqq.config;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson2.JSONObject;
import io.juneqqq.cache.CacheConstant;
import io.juneqqq.constant.RocketMQConstant;
import io.juneqqq.pojo.dao.entity.UserFollowing;
import io.juneqqq.pojo.dao.entity.UserMoment;
import io.juneqqq.service.common.UserFollowingService;
import io.juneqqq.service.websocket.WebSocketService;
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

import jakarta.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
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
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_TEST);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

//    @Bean
//    public DefaultMQPushConsumer testConsumer() throws Exception {
//        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_TEST);
//        consumer.setNamesrvAddr(nameServerAddr);
//        consumer.subscribe(RocketMQConstant.TOPIC_TEST, "*");
//        consumer.registerMessageListener(new MessageListenerConcurrently() {
//            @Override
//            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
//                log.info(RocketMQConstant.GROUP_TEST + "???????????????" + messages);
//                for (MessageExt message : messages) {
//                    log.info(Arrays.toString(message.getBody()));
//                }
//                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
//            }
//        });
//        consumer.start();
//        return consumer;
//    }


    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws Exception {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_MOMENTS);
        producer.setNamesrvAddr(nameServerAddr);
        producer.start();
        return producer;
    }

    /**
     * ????????????????????????set??????????????????
     */
    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws Exception {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_MOMENTS);
        consumer.setNamesrvAddr(nameServerAddr);
        consumer.subscribe(RocketMQConstant.TOPIC_MOMENTS, "*");
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                MessageExt msg = messages.get(0);
                Optional.ofNullable(msg).ifPresentOrElse(m -> {
                    assert msg != null;
                    log.debug("???????????????momentsConsumer??????" + new String(msg.getBody()));
                    String bodyStr = new String(msg.getBody());
                    UserMoment userMoment = JSONObject.parseObject(bodyStr, UserMoment.class);
                    Long userId = userMoment.getUserId();
                    List<UserFollowing> fanList = userFollowingService.getUserFanInfos(userId);
                    // ???????????????????????????
                    if (CollUtil.isEmpty(fanList)) fanList = new ArrayList<>();
                    fanList.add(UserFollowing.builder().userId(userId).build());

                    for (UserFollowing fan : fanList) {
                        // ???????????????????????????????????????????????????
                        String key = CacheConstant.USER_SUBSCRIBED + fan.getUserId();
                        stringRedisTemplate.opsForSet().add(key, bodyStr);
                    }
                }, () -> log.warn("???????????????null?????????messages:" + messages));

                log.debug("\n??????????????????");
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        consumer.start();
        return consumer;
    }

    @Bean("danmusProducer")
    public DefaultMQProducer danmusProducer() throws Exception {
        // ????????????????????????Producer
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.GROUP_DANMUS);
        // ??????NameServer?????????
        producer.setNamesrvAddr(nameServerAddr);
        // ??????Producer??????
        producer.start();
        return producer;
    }

    @Bean("danmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws Exception {
        // ??????????????????
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.GROUP_DANMUS);
        // ??????NameServer?????????
        consumer.setNamesrvAddr(nameServerAddr);
        // ????????????????????????Topic?????????Tag??????????????????????????????
        consumer.subscribe(RocketMQConstant.TOPIC_DANMU, "*");
        // ?????????????????????????????????broker?????????????????????
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
                MessageExt msg = messages.get(0);
                log.debug("danmu?????????????????????{}",msg);
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
                }, () -> log.warn("???????????????null?????????messages:" + messages));

                log.debug("danmu??????????????????~");
                // ????????????????????????????????????
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        // ?????????????????????
        consumer.start();
        return consumer;
    }
}
