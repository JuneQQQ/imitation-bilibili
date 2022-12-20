package io.juneqqq.constant;


import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RocketMQConstant {
    public static final String GROUP_MOMENTS = "MomentsGroup";
    public static final String GROUP_TEST = "TestGroup";
    public static final String GROUP_DANMUS = "DanmusGroup";
    public static final String TOPIC_MOMENTS = "TopicMoments";
    public static final String TOPIC_DANMU = "TopicDanmus";
    public static final String TOPIC_TEST = "TopicTest";

    public static final String GROUP_CACHE_UPDATE = "GroupCacheUpdate";
    public static final String TOPIC_CACHE_UPDATE = "TopicCacheUpdate";
}
