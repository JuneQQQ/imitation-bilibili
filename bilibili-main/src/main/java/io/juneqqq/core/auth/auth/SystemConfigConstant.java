package io.juneqqq.core.auth.auth;

public class SystemConfigConstant {
    private SystemConfigConstant() {
    }
    /**
     * Http 请求认证 Header
     */
    public static final String HTTP_AUTH_HEADER_NAME = "Authorization";

    /**
     * 前台门户系统标识
     */
    public static final String BILIBILI_FRONT_KEY = "front";

    /**
     * 用户管理系统标识
     */
    public static final String BILIBILI_AUTHOR_KEY = "author";

    /**
     * 后台管理系统标识
     */
    public static final String BILIBILI_ADMIN_KEY = "admin";

    /**
     * 图片上传目录
     */
    public static final String IMAGE_UPLOAD_DIRECTORY = "/image/";

    /**
     * 常量类实例化异常信息
     */
    public static final String CONST_INSTANCE_EXCEPTION_MSG = "Constant class";
}
