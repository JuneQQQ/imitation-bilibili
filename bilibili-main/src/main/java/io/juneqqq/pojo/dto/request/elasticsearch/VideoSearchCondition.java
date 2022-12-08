package io.juneqqq.pojo.dto.request.elasticsearch;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 视频检索条件
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class VideoSearchCondition extends BasePageSearchCondition {
    @Schema(description = "关键字")
    private String keyword;

    // Video 检索条件开始 =====================
    @Schema(description = "视频最小时长,默认0")
    private Integer minDuration;

    @Schema(description = "视频最大时长,默认 10240000s 2844h")
    private Integer maxDuration;

    @Schema(description = "分区 0其他 1音乐 2电影 3游戏 4鬼畜 5番剧",
            allowableValues = {"0", "1", "2", "3", "4", "5"})
    private Integer partition;


    public Integer getMinDuration() {
        return minDuration == null ? 0 : minDuration;
    }

    public Integer getMaxDuration() {
        return maxDuration == null ? Integer.MAX_VALUE : maxDuration;
    }
}

