package io.juneqqq.core.auth;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AuthElementOperation {

    private Long id;

    private String elementName;

    private String elementCode;

    private String operationType;

    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill= FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
