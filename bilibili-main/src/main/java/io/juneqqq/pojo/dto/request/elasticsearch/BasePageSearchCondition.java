package io.juneqqq.pojo.dto.request.elasticsearch;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
public class BasePageSearchCondition {
    @Schema(description = "当前页码,from 0")
    private Integer current;

    @Schema(description = "总记录数")
    private Integer total;

    @Schema(description = "每页记录数")
    private Integer size;

    @Schema(description = "排序字段")
    private String[] sort;
    @Schema(description = "是否升序排列，与上面sort字段一一对应")
    private Boolean[] isAsc;

    public Integer getCurrent() {
        return current == null ? DefaultPageParam.DEFAULT_CURRENT : current;
    }

    public Integer getTotal() {
        return total;
    }

    public Integer getSize() {
        return size == null ? DefaultPageParam.DEFAULT_SIZE : size;
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    static class DefaultPageParam {
        public static final Integer DEFAULT_SIZE = 10;
        public static final Integer DEFAULT_CURRENT = 0; // 默认从0开始
    }

}
