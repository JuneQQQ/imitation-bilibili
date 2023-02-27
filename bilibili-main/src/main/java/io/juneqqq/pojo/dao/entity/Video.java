package io.juneqqq.pojo.dao.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Video {

    @Id
    @Schema(description = "id")
    private Long id;

    @Schema(description = "用户id")
    private Long userId;
    @Schema(description = "文件id")
    private Long fileId;
    @Schema(description = "封面链接")
    private String cover;
    @Schema(description = "")
    private String title;
    @Schema(description = "视频类型 0其他 1原创 2转载 3翻译")
    private Integer type;
    @Schema(description = " 分区 0其他 1音乐 2电影 3游戏 4鬼畜 5番剧")
    @TableField("`partition`")
    private Integer partition;
    @Schema(description = "时长")
    private Integer duration;
    @Schema(description = "视频简介/描述")
    private String description;

    @Schema(description = "创建时间")
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @Schema(description = "最近修改时间")
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    @Schema(description = "标签列表")
    @TableField(exist = false)
    private List<VideoTag> videoTagList; // 标签列表
}
