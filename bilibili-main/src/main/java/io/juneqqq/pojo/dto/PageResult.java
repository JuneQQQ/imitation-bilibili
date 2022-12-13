package io.juneqqq.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;


/**
 * 分页概念类
 *
 * @param list 分页数据集
 */
public record PageResult<T>(
        @Schema(description = "总记录数") long total,
        @Schema(description = "页码") long current,
        @Schema(description = "每页大小") long size,
        @Schema(description = "分页数据集") List<T> list,
        @Schema(description = "整块自定义数据") T data
) {
    public PageResult(long total, long current, long size, List<T> list, T data) {
        this.total = total;
        this.current = current;
        this.size = size;
        this.list = list;
        this.data = data;
    }

    public static <T> PageResult<T> of(long current, long size, long total, List<T> list) {
        return new PageResult<>(total, current, size, list, null);
    }

    public static <T> PageResult<T> of(long current, long size, long total, T data) {
        return new PageResult<>(total, current, size, null, data);
    }
}
