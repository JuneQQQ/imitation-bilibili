package io.juneqqq.pojo.dto.database;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "点赞、投币、收藏")
public class VideoLCC {
    @Schema(description = "video id")
    private Integer videoId;
    @Schema(description = "点赞量")
    private Integer like = 0;
    @Schema(description = "投币量")
    private Integer coin = 0;
    @Schema(description = "收藏")
    private Integer collection = 0;
}
