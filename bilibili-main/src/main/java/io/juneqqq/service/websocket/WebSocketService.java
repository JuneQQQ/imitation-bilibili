package io.juneqqq.service.websocket;

import com.alibaba.fastjson2.JSONObject;
import io.juneqqq.constant.RocketMQConstant;
import io.juneqqq.core.auth.auth.UserHolder;
import io.juneqqq.dao.entity.Danmu;
import io.juneqqq.service.common.DanmuService;
import io.juneqqq.util.RocketMQUtil;
import io.netty.util.internal.StringUtil;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * WebSocket 默认多实例！
 */
@Component
@ServerEndpoint("/imserver/{token}")
public class WebSocketService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    public static final ConcurrentHashMap<String, WebSocketService> ONLINE_SESSION_MAP = new ConcurrentHashMap<>();

    private Session session;

    private String sessionId;

    private Long userId;

    private static ApplicationContext APPLICATION_CONTEXT;

    public static void exposeApplicationContext(ApplicationContext applicationContext) {
        WebSocketService.APPLICATION_CONTEXT = applicationContext;
    }

    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        // websocket没有请求头
        this.userId = UserHolder.getUserId(token);
        this.sessionId = session.getId();
        this.session = session;
        if (ONLINE_SESSION_MAP.containsKey(sessionId)) {
            ONLINE_SESSION_MAP.remove(sessionId);
            ONLINE_SESSION_MAP.put(sessionId, this);
        } else {
            ONLINE_SESSION_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        logger.info("用户连接成功：{}，当前在线人数为：{}", sessionId, ONLINE_COUNT.get());
        try {
            this.sendMessage("0");
        } catch (Exception e) {
            logger.error("连接异常");
        }
    }

    @OnClose
    public void closeConnection(Session session) {
        logger.info("Session：{}", session);
        logger.info("SessionId：{}", session.getId());

        if (ONLINE_SESSION_MAP.containsKey(sessionId)) {
            ONLINE_SESSION_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        logger.debug("用户退出：{},当前在线人数为：{}", sessionId, ONLINE_COUNT.get());
    }

    @OnMessage
    public void onMessage(String message) {
        logger.debug("用户信息：{},报文：{}", sessionId, message);
        if (!StringUtil.isNullOrEmpty(message)) {
            try {
                //群发消息
                for (Map.Entry<String, WebSocketService> entry : ONLINE_SESSION_MAP.entrySet()) {
                    WebSocketService webSocketService = entry.getValue();
                    // 交给MQ去群发弹幕
                    DefaultMQProducer danmusProducer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("message", message);
                    jsonObject.put("sessionId", webSocketService.getSessionId());
                    Message msg = new Message(RocketMQConstant.TOPIC_DANMU, jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8));
                    RocketMQUtil.asyncSendMsg(danmusProducer, msg);
                }
                // 当用户已登录时
                if (this.userId != null) {
                    Danmu danmu = JSONObject.parseObject(message, Danmu.class);
                    danmu.setUserId(userId);
                    DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuServiceImpl");
                    // @Async + @EnableAsync
                    logger.debug("即将保存弹幕到database&redis：{}", danmu);
                    // 异步存库
                    danmuService.asyncAddDanmu(danmu);
                    // 存redis
                    danmuService.addDanmusToRedis(danmu);
                }
            } catch (Exception e) {
                logger.error("弹幕接收出现问题，异常打印如下：{}",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @OnError
    public void onError(Throwable error) {
        logger.error("\n未知异常：" + error.getClass());
        error.printStackTrace();
    }

    public void sendMessage(String message) throws IOException {
        logger.debug("我将要发送websocket消息:{}", message);
        this.session.getBasicRemote().sendText(message);
    }

    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate = 5000)
    private void noticeOnlineCount() throws IOException {
        for (Map.Entry<String, WebSocketService> entry : WebSocketService.ONLINE_SESSION_MAP.entrySet()) {
            WebSocketService webSocketService = entry.getValue();
            if (webSocketService.session.isOpen()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("onlineCount", ONLINE_COUNT.get());
                jsonObject.put("msg", "当前在线人数为" + ONLINE_COUNT.get());
                jsonObject.put("code", 23001);
                webSocketService.sendMessage(jsonObject.toJSONString());
            }
        }
    }

    public Session getSession() {
        return session;
    }

    public String getSessionId() {
        return sessionId;
    }
}
