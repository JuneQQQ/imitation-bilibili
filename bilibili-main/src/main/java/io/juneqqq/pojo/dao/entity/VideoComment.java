package io.juneqqq.pojo.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class VideoComment {

    private Long id;

    private Long videoId;

    private Long userId;

    private String comment;

    private Long replyUserId;  // 回复目标用户的id

    private Long parentId;

    @TableField(exist = false)
    private List<VideoComment> childList;

    @TableField(exist = false)
    private UserInfo userInfo;

    @TableField(exist = false)
    private UserInfo replyUserInfo;


    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
