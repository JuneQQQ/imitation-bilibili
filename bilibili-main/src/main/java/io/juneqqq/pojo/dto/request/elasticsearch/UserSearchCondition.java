package io.juneqqq.pojo.dto.request.elasticsearch;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 用户检索条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserSearchCondition extends BasePageSearchCondition {
    @Schema(description = "关键字")
    private String keyword;

    // User 检索条件开始 =======================
    @Schema(description = "性别")
    private Integer gender;
    @Schema(description = "min等级")
    private Integer minLevel;
    @Schema(description = "max等级")
    private Integer maxLevel;
    @Schema(description = "最低粉丝数")
    private Integer minFanCount;
    @Schema(description = "最高粉丝数")
    private Integer maxFanCount;

    public Integer getMinLevel() {
        return minLevel == null ? 0 : minLevel;
    }

    public Integer getMaxLevel() {
        return maxLevel == null ? Integer.MAX_VALUE : maxLevel;
    }

    public Integer getMinFanCount() {
        return minFanCount == null ? 0 : minFanCount;
    }

    public Integer getMaxFanCount() {
        return maxFanCount == null ? Integer.MAX_VALUE : maxFanCount;
    }
}

