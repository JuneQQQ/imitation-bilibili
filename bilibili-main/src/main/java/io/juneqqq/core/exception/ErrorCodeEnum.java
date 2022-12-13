package io.juneqqq.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCodeEnum {

    /**
     * 正确执行后的返回
     */
    OK("200", "一切 ok"),

    /**
     * 一级宏观错误码，用户端错误
     */
    USER_ERROR("A0001", "用户端错误"),

    /**
     * 二级宏观错误码，用户注册错误
     */
    USER_REGISTER_ERROR("A0100", "用户注册错误"),

    /**
     * 用户未同意隐私协议
     */
    USER_NO_AGREE_PRIVATE_ERROR("A0101", "用户未同意隐私协议"),

    /**
     * 注册国家或地区受限
     */
    USER_REGISTER_AREA_LIMIT_ERROR("A0102", "注册国家或地区受限"),

    /**
     * 用户验证码错误
     */
    USER_VERIFY_CODE_ERROR("A0240", "用户验证码错误"),

    /**
     * 空手机号
     */
    USER_REGISTER_PHONE_IS_NULL("A103","空的手机号？"),

    /**
     * 手机号已被注册
     */
    USER_REGISTER_PHONE_HAS_EXISTS("A104","手机号已被注册"),
    /**
     * 密码解密失败
     */
    USER_REGISTER_PASSWORD_DECODE_EXCEPTION("A105","密码解密失败"),

    /**
     * 用户名已存在
     */
    USER_NAME_EXIST("A0111", "用户名已存在"),

    /**
     * 用户账号不存在
     */
    USER_ACCOUNT_NOT_EXIST("A0201", "用户账号不存在"),

    /**
     * 用户密码错误
     */
    USER_PASSWORD_ERROR("A0210", "用户密码错误"),

    /**
     * 前台密码解密失败
     */
    USER_PASSWORD_DECODE_ERROR("A0211", "前台密码解密失败"),

    /**
     * 用户登录已过期
     */
    USER_LOGIN_EXPIRED("A0230", "用户登录已过期"),

    /**
     * 访问未授权
     */
    USER_UN_AUTH("A0301", "访问未授权"),

    /**
     * 用户请求服务异常
     */
    USER_REQ_EXCEPTION("A0500", "用户请求服务异常"),

    /**
     * 请求超出限制
     */
    USER_REQ_MANY("A0501", "请求超出限制"),

    /**
     * 用户评论异常
     */
    USER_COMMENT("A2000", "用户评论异常"),

    /**
     * 用户评论异常
     */
    USER_COMMENTED("A2001", "用户已发表评论"),

    /**
     * 用户发布异常
     */
    AUTHOR_PUBLISH("A3000", "用户发布异常"),

    TARGET_GROUP_NOT_EXISTS("A4000","目标分组不存在"),
    TARGET_USER_NOT_EXISTS("A4001","目标用户不存在"),
    TARGET_VIDEO_NOT_EXISTS("A4002","目标视频不存在"),


    USER_MOMENT_ID_HAS_EXISTED("A4050","user_moment id已存在，不能重复插入"),
    VIDEO_HAS_BEEN_LIKE_BY_SAME_USER("A4051","用户已点赞过"),
    COIN_OUT_OF_LIMIT("A4052","最大可投3个币"),
    COIN_NOT_ENOUGH("A4053","没有足够的硬币"),


    /**
     * 用户上传文件异常
     */
    FILE_UPLOAD_ERROR("A0700", "用户上传文件异常"),

    /**
     * 用户上传文件类型不匹配
     */
    FILE_TYPE_NOT_MATCH("A0701", "用户上传文件类型不匹配"),
    /**
     * offset大于fileSize
     */
    FILE_OFFSET_TOO_LARGE("A0702","offset大于fileSize"),

    /**
     * elastic search 搜索异常
     */
    SEARCH_ERROR("A0900","ElasticSearch搜索异常，具体请查看日志"),


    /**
     * 文件上传异常
     */
    MINIO_FILE_IO_ERROR("A1000", "文件io异常"),
    MINIO_SERVER_ERROR("A1001", "文件服务器异常"),
    MINIO_INSUFFICIENT_DATA("A1002", "文件服务器资源不足"),
    MINIO_FILE_NAME_NOT_MATCH_HASH("A1003","之前已经上传过，但文件名不一致，minio无法修改，故已删除minio文件，请重新上传"),
    MINIO_UNKNOW_EXCEPTION("A1100", "MINIO上传发生了未知异常"),

    /**
     * MQ异常
     */
    MQ_MESSAGE_SEND_FAILED("A1200","MQ消息未发送成功"),

    /**
     * 二级宏观错误码，系统执行超时
     */
    SYSTEM_TIMEOUT_ERROR("B0100", "系统执行超时"),


    /**
     * 一级宏观错误码，系统执行出错
     */
    SYSTEM_ERROR("B0001", "系统执行出错"),

    /**
     * 一级宏观错误码，调用第三方服务出错
     */
    THIRD_SERVICE_ERROR("C0001", "调用第三方服务出错"),

    /**
     * 一级宏观错误码，中间件服务出错
     */
    MIDDLEWARE_SERVICE_ERROR("C0100", "中间件服务出错"),
    UNKNOW_EXCEPTION("C0000", "未知异常");

    /**
     * 错误码
     */
    final String code;

    /**
     * 中文描述
     */
    final String message;
}
