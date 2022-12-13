package io.juneqqq.pojo.dto.cache;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author june
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CacheUserInfoDto implements Serializable {

    /**
     * UserInfo 表id
     */
    private Long id;

    private Long userId;

    private String nick;

    private String avatar;

    private String sign;

    private Integer gender;

    private LocalDate birth;

    private Integer coin;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
