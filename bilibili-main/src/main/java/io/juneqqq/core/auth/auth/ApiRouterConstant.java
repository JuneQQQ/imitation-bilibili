package io.juneqqq.core.auth.auth;


public class ApiRouterConstant {
    private ApiRouterConstant() {
        throw new IllegalStateException(SystemConfigConstant.CONST_INSTANCE_EXCEPTION_MSG);
    }

    /**
     * API请求路径前缀
     */
    public static final String API_URL_PREFIX = "/api";

    /**
     * 前台门户系统请求路径前缀
     */
    public static final String API_FRONT_URL_PREFIX = API_URL_PREFIX + "/front";
    /**
     * 平台后台管理系统请求路径前缀
     */
    public static final String API_ADMIN_URL_PREFIX = API_URL_PREFIX + "/admin";

    /**
     * 首页模块请求路径前缀
     */
    public static final String HOME_URL_PREFIX = "/home";

    /**
     * 用户模块请求路径前缀
     */
    public static final String USER_URL_PREFIX = "/user";


    /**
     * 视频模块请求路径前缀
     */
    public static final String VIDEO_URL_PREFIX = "/video";

    /**
     * 弹幕模块请求路径前缀
     */
    public static final String DANMU_URL_PREFIX = "/danmu";

    /**
     * 资源（图片/视频/文档）模块请求路径前缀
     */
    public static final String RESOURCE_URL_PREFIX = "/resource";

    /**
     * 搜索模块请求路径前缀
     */
    public static final String SEARCH_URL_PREFIX = "/search";

    /**
     * 前台门户首页API请求路径前缀
     */
    public static final String API_FRONT_HOME_URL_PREFIX = API_FRONT_URL_PREFIX + HOME_URL_PREFIX;
    /**
     * 前台视频API请求路径前缀
     */
    public static final String API_FRONT_VIDEO_URL_PREFIX = API_FRONT_URL_PREFIX + VIDEO_URL_PREFIX;
    /**
     * 前台弹幕API请求路径前缀
     */
    public static final String API_FRONT_DANMU_URL_PREFIX = API_FRONT_URL_PREFIX + DANMU_URL_PREFIX;

    /**
     * 前台门户用户相关API请求路径前缀
     */
    public static final String API_FRONT_USER_URL_PREFIX = API_FRONT_URL_PREFIX + USER_URL_PREFIX;
    /**
     * 前台门户资源（图片/视频/文档）相关API请求路径前缀
     */
    public static final String API_FRONT_RESOURCE_URL_PREFIX = API_FRONT_URL_PREFIX + RESOURCE_URL_PREFIX;

    /**
     * 前台门户搜索相关API请求路径前缀
     */
    public static final String API_FRONT_SEARCH_URL_PREFIX =
            API_FRONT_URL_PREFIX + SEARCH_URL_PREFIX;
}
