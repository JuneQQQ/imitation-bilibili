package io.juneqqq.pojo.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_demo")
public class Demo {
    // 默认策略：雪花算法+UUID
    // AUTO自增
    // INPUT
    @TableId(type = IdType.ASSIGN_ID)
    Integer id;

    String demoA;
    String demoB;

    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
