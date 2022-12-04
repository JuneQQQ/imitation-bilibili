package com.juneqqq.controller;


import com.juneqqq.entity.constant.UserMomentsConstant;
import com.juneqqq.util.RocketMQUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RocketMQTest {
    @Resource
    DefaultMQProducer testProducer;

    @Resource
    DefaultMQPushConsumer testConsumer;


    @Test
    void testProduce() throws Exception {
        for (int i = 0; i < 1000; i++) {
            Message message = new Message();
            message.setBody(("this is a test message,seq:"+i).getBytes());
            message.setTopic(UserMomentsConstant.TOPIC_TEST);
            RocketMQUtil.asyncSendMsg(testProducer,message);
        }
    }
    @Test
    void testConsume() throws Exception {
//        Message message = new Message();
//        message.setBody("this is a test message".getBytes());
//        message.setTopic(UserMomentsConstant.TOPIC_TEST);
//        RocketMQUtil.asyncSendMsg(testProducer,message);
    }


    @Test
    void testLoads(){
        log.info("success");
    }
}
