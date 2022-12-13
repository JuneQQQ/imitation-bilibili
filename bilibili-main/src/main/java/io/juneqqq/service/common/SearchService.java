package io.juneqqq.service.common;

import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.pojo.dto.request.elasticsearch.UserSearchCondition;
import io.juneqqq.pojo.dto.request.elasticsearch.VideoSearchCondition;
import io.juneqqq.pojo.dto.response.elasticsearch.UserSearchResult;
import io.juneqqq.pojo.dto.response.elasticsearch.VideoSearchResult;

public interface SearchService {
    PageResult<VideoSearchResult> searchVideos(VideoSearchCondition condition);

    PageResult<UserSearchResult> searchUserInfos(UserSearchCondition condition);

}
