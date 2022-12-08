package io.juneqqq.dao.entity;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserMoment {

    private Long id;

    private Long userId;

    private String type; // 动态类型，0视频 1直播 2专栏动态 3普通用户动态

    private Long contentId; //内容id，与type字段深度耦合，0->查视频表，1->查直播表...

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
