package io.juneqqq.pojo.dto.cache;
import io.juneqqq.dao.entity.VideoTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 本地/Redis缓存对象，目前是跟es存储对象一模一样的
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CacheVideoInfoDto {
    private Long id;
    private Long userId;
    private Long fileId;
    private String nick; // 用户昵称【可检索】
    private String title; // 标题【检索重点】
    private String description; // 描述【检索 比重小】
    private String cover; // 封面链接
    private Integer type; // 视频类型 0其他 1原创 2转载 3翻译
    private Integer partition; // 分区 0其他 1音乐 2电影 3游戏 4鬼畜 5...
    private Integer like; // 点赞量
    private Integer coin; // 投币量
    private Integer collection; // 收藏
    private Integer duration;  // 时长
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private List<VideoTag> videoTagList; // 标签列表
}
