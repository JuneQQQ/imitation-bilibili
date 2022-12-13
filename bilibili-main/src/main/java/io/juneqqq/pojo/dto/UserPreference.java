package io.juneqqq.pojo.dto;

import lombok.Data;

/**
 * 协同推荐DTO
 */
@Data
public class UserPreference {
    private Long userId;

    private Long videoId;

    private Float value;

}
