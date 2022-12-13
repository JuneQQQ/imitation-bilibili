package io.juneqqq.dao.repository.esmodel;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import io.juneqqq.constant.elastic.UserInfoIndex;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = UserInfoIndex.NAME)
public class EsUserInfoDto {
    @Id
    @Field(type = FieldType.Long)
    private Long id;  // UserInfo表id
    @Field(type = FieldType.Long)
    private Long userId;
    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String nick;
    @Field(type = FieldType.Text, searchAnalyzer = "ik_smart", analyzer = "ik_smart")
    private String sign;
    @Field(type = FieldType.Keyword, index = false, docValues = false)
    private String avatar;
    @Field(type = FieldType.Integer)
    private Integer gender;
    @Field(type = FieldType.Long)
    private Integer coin;
    @Field(type = FieldType.Date,format = DateFormat.date)
    private LocalDate birth;
    @Field(type = FieldType.Boolean)
    private Boolean isVip;
    @Field(type = FieldType.Byte)
    private Integer level;
    @Field(type = FieldType.Integer)
    private Integer fanCount;
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createTime;

    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;
}
