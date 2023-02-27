package io.juneqqq.pojo.dto.response.elasticsearch;


import io.juneqqq.pojo.dao.entity.VideoTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoSearchResult {
    @Schema(description = "video 表 id")
    private Long id;
    @Schema(description = "标题")
    private String title;
    @Schema(description = "视频作者昵称")
    private String nick;
    @Schema(description = "视频描述/简介")
    private String description;

    @Schema(description = "分区 0其他 1音乐 2电影 3游戏 4鬼畜 5...",
            allowableValues = {"0", "1", "2", "3","4","5"})
    private Integer partition;
    @Schema(description = "视频时长")
    private Integer duration;
    @Schema(description = "视频作者id")
    private Long userId;
    @Schema(description = "文件id")
    private Long fileId;
    @Schema(description = "封面链接")
    private String cover;
    @Schema(description = "视频类型 0其他 1原创 2转载 3翻译,",
            allowableValues = {"0", "1", "2", "3"})
    private Integer type;
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "标签列表")
    private List<VideoTag> videoTagList; // 标签列表

}
