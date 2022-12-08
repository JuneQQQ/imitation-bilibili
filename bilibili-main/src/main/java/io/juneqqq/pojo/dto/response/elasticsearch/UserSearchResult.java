package io.juneqqq.pojo.dto.response.elasticsearch;

import lombok.Data;

import java.time.LocalDate;

/**
 * 返回前台 vo  注意严格与dto对象保持一致
 */
@Data
public class UserSearchResult {
    private String nick;
    private Integer level;
    private String avatar;
    private String sign;
    private LocalDate birth;
    private Integer fanCount;
    private Boolean isVip;
}
