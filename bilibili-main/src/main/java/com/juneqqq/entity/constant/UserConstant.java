package com.juneqqq.entity.constant;

import cn.hutool.core.util.IdUtil;

public interface UserConstant {
    Integer GENDER_MALE = 0;

    Integer GENDER_FEMALE = 1;

    Integer GENDER_SECRET = 3;

    Integer GENDER_UNKNOW = 4;

    String DEFAULT_BIRTH = "2001-07-20";

    String DEFAULT_NICK = "default-" + IdUtil.simpleUUID().substring(0,6);

    String USER_FOLLOWING_GROUP_TYPE_DEFAULT = "2";

    String USER_FOLLOWING_GROUP_TYPE_USER = "3";

    String USER_FOLLOWING_GROUP_ALL_NAME = "全部关注";

    Long DEFAULT_USER_PAGE_SIZE = 20L;
    Long DEFAULT_USER_PAGE_NO = 1L;

    String USER_REFRESH_TOKEN_PREFIX = "user:refreshToken:";
    Integer USER_REFRESH_TOKEN_TIMEOUT_SECONDS = 60 * 60 * 24 * 7; // 7day
    String DEFAULT_AVATAR = "https://default-1309509880.cos.ap-nanjing.myqcloud.com/uPic/-787dae1a3fccd3ca.jpg";
    String DEFAULT_SIGN = "和优秀的人，做有挑战的事！";
}
