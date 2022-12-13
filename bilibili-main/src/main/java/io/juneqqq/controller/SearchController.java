package io.juneqqq.controller;


import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.dao.entity.R;
import io.juneqqq.pojo.dto.request.elasticsearch.UserSearchCondition;
import io.juneqqq.pojo.dto.request.elasticsearch.VideoSearchCondition;
import io.juneqqq.pojo.dto.response.elasticsearch.UserSearchResult;
import io.juneqqq.pojo.dto.response.elasticsearch.VideoSearchResult;
import io.juneqqq.service.common.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@Tag(name = "SearchController", description = "搜索模块")
@RequestMapping(ApiRouterConstant.API_FRONT_SEARCH_URL_PREFIX)
public class SearchController {
    @Resource
    private SearchService elasticSearchService;

    @Operation(description = "查询用户")
    @GetMapping("/user-infos")
    public R<PageResult<UserSearchResult>> searchVideo(
            @ParameterObject UserSearchCondition condition
    ) {
        PageResult<UserSearchResult> contents = elasticSearchService.searchUserInfos(condition);
        return R.ok(contents);
    }
    @Operation(description = "查询视频")
    @GetMapping("/videos")
    public R<PageResult<VideoSearchResult>> searchVideo(
            @ParameterObject VideoSearchCondition condition
    ) {
        PageResult<VideoSearchResult> contents = elasticSearchService.searchVideos(condition);
        return R.ok(contents);
    }


}
