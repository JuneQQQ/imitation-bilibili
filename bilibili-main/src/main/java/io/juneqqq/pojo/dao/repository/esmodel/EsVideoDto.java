package io.juneqqq.pojo.dao.repository.esmodel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.juneqqq.constant.elastic.VideoIndex;
import io.juneqqq.pojo.dao.entity.VideoTag;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = VideoIndex.NAME)
public class EsVideoDto {
    @Id
    @Field(type = FieldType.Long, index = false)
    private Long id;
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Long)
    private Long fileId;
    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String nick; // 用户昵称【可检索】
    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String title; // 标题【检索重点】
    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String description; // 描述【检索 比重小】
    @Field(type = FieldType.Keyword, docValues = false, index = false)
    private String cover; // 封面链接
    @Field(type = FieldType.Integer)
    private Integer type; // 视频类型 0其他 1原创 2转载 3翻译
    @Field(type = FieldType.Integer)
    private Integer partition; // 分区 0其他 1音乐 2电影 3游戏 4鬼畜 5...

    @Field(type = FieldType.Integer)
    private Integer like; // 点赞量
    @Field(type = FieldType.Integer)
    private Integer coin; // 投币量
    @Field(type = FieldType.Integer)
    private Integer collection; // 收藏
    @Field(type = FieldType.Integer)
    private Integer duration;  // 时长
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second_millis)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
    private List<VideoTag> videoTagList; // 标签列表

}
