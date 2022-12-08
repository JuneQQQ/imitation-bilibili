package io.juneqqq.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Configuration;


@Configuration // 增加整个消息转换器会导致swagger出问题
public class CustomObjectMapper extends ObjectMapper {
    public CustomObjectMapper() {
        super();
        // 设置日期转换yyyy-MM-dd HH:mm:ss
        // 序列换成json时,将所有的long变成string,因为js中得数字类型不能包含所有的java long值
        registerModule(new JavaTimeModule());
        setSerializationInclusion(JsonInclude.Include.NON_NULL); // null不序列化
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false); // 忽略不匹配属性
        SimpleModule simpleModule = new SimpleModule("LongModule");
        simpleModule.addSerializer(Long.class, ToStringSerializer.instance);
        simpleModule.addSerializer(Long.TYPE, ToStringSerializer.instance);
        registerModule(simpleModule);
    }
}