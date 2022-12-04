package com.juneqqq.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;

@Slf4j
public class RocketMQUtil {

    public static void syncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{
        SendResult result = producer.send(msg);
        log.debug("同步消息已发送消息："+ new String(msg.getBody()));
        log.debug("发送结果："+result);
    }

    public static void asyncSendMsg(DefaultMQProducer producer, Message msg) throws Exception{
        producer.send(msg, new SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                log.debug("异步发送消息已发送：" + new String(msg.getBody()));
                log.debug("发送结果：" + result);
            }
            @Override
            public void onException(Throwable e) {
                log.error("消息发送失败！");
                e.printStackTrace();
            }
        });
    }
}
