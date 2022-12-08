package io.juneqqq.pojo.dto.response.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonSearchResult {
    private List<VideoSearchResult> videoSearchResultList;
    private List<UserSearchResult> userSearchResultList;
}
