package com.juneqqq.entity.dao;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VideoTag {

    private Long id;

    private Long videoId;

    private Long tagId;

    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;

}
