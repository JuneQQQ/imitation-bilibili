package com.juneqqq.entity.dao;


import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(indexName = "videos")
public class Video {

    @Id
    @Field(type = FieldType.Long)
    private Long id;

    @Field(type = FieldType.Long)
    private Long userId;

    @Field(type = FieldType.Long)
    private Long fileId;

    private String cover; // 封面链接

    @Field(type = FieldType.Text)
    private String title;

    private Integer type; // 视频类型 0其他 1原创 2转载 3翻译

    @TableField("`partition`")  // partition是mysql关键字！！
    private Integer partition; // 分区 0其他 1音乐 2电影 3游戏 4鬼畜 5...

    private String duration;  // 时长

    @Field(type = FieldType.Text)
    private String description;


    @TableField(fill = FieldFill.INSERT)
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Field(type = FieldType.Date,format = DateFormat.date_hour_minute_second_millis)
    private LocalDateTime updateTime;


    @TableField(exist = false)
    private List<VideoTag> videoTagList; // 标签列表

}
