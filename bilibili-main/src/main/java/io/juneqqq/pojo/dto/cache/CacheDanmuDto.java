package io.juneqqq.pojo.dto.cache;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CacheDanmuDto {

    private Long id;

    private Long userId;

    private Long videoId;

    private String content;

    private String timestamp;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
