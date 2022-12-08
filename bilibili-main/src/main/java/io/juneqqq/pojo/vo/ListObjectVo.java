package io.juneqqq.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ListObjectVo {
    private String objectName;
    private String etag;
}
