package io.juneqqq.dao.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Danmu {

    private Long id;

    private Long userId;

    private Long videoId;

    private String content;

    private String timestamp;

    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
