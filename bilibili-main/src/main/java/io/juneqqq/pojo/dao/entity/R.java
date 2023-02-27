package io.juneqqq.pojo.dao.entity;

import io.juneqqq.core.exception.ErrorCodeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Objects;

@Getter
public class R<T> {

    /**
     * 响应码
     */
    @Schema(description = "错误码，00000-没有错误")
    private final String code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息")
    private final String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    private R() {
        this.code = ErrorCodeEnum.OK.getCode();
        this.message = ErrorCodeEnum.OK.getMessage();
    }

    private R(ErrorCodeEnum errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }

    private R(T data) {
        this();
        this.data = data;
    }

    /**
     * 业务处理成功,无数据返回
     */
    public static R<Void> ok() {
        return new R<>();
    }

    /**
     * 业务处理成功，有数据返回
     */
    public static <T> R<T> ok(T data) {
        return new R<>(data);
    }

    /**
     * 业务处理失败
     */
    public static R<Void> fail(ErrorCodeEnum errorCode) {
        return new R<>(errorCode);
    }
    /**
     * 系统错误
     */
    public static R<Void> error() {
        return new R<>(ErrorCodeEnum.SYSTEM_ERROR);
    }

    /**
     * 判断是否成功
     */
    public boolean isOk() {
        return Objects.equals(this.code, ErrorCodeEnum.OK.getCode());
    }
}
