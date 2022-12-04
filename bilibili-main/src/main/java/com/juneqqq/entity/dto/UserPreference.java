package com.juneqqq.entity.dto;

import lombok.Data;

@Data
public class UserPreference {

//    private Long id;

    private Long userId;

    private Long videoId;

    private Float value;

//    @TableField(fill= FieldFill.INSERT)
//    private LocalDateTime createTime;
//    @TableField(fill= FieldFill.INSERT_UPDATE)
//    private LocalDateTime updateTime;

}
