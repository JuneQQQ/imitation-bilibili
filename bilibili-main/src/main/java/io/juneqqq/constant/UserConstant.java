package io.juneqqq.constant;

import cn.hutool.core.util.IdUtil;

import java.time.LocalDate;

public class UserConstant {
    public static final Integer GENDER_MALE = 0;
    public static final Integer DEFAULT_LEVEL = 1;
    public static final LocalDate DEFAULT_BIRTH = LocalDate.parse("2001-07-20");
    public static final String DEFAULT_NICK = "default-" + IdUtil.simpleUUID().substring(0, 6);
    public static final String USER_FOLLOWING_GROUP_TYPE_DEFAULT = "2";
    public static final String USER_FOLLOWING_GROUP_TYPE_USER = "3";
    public static final String USER_FOLLOWING_GROUP_ALL_NAME = "全部关注";
    public static final String USER_REFRESH_TOKEN_PREFIX = "user:refreshToken:";
    public static final Integer USER_REFRESH_TOKEN_TIMEOUT_SECONDS = 60 * 60 * 24 * 7; // 7day
    public static final String DEFAULT_AVATAR = "https://default-1309509880.cos.ap-nanjing.myqcloud.com/uPic/-787dae1a3fccd3ca.jpg";
    public static final String DEFAULT_SIGN = "和优秀的人，做有挑战的事！";
}
