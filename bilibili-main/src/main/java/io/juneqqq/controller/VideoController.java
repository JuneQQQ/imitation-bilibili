package io.juneqqq.controller;

import com.alibaba.fastjson.JSONObject;

import com.alibaba.fastjson2.JSON;
import io.juneqqq.core.auth.auth.ApiRouterConstant;
import io.juneqqq.core.auth.auth.UserHolder;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.service.common.SearchService;
import io.juneqqq.service.common.VideoService;

import io.juneqqq.util.UserSupport;
import io.juneqqq.dao.entity.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@Tag(name = "VideoController", description = "视频模块")
@RequestMapping(ApiRouterConstant.API_FRONT_VIDEO_URL_PREFIX)
public class VideoController {

    @Resource
    private VideoService videoService;

    @Resource
    private UserSupport userSupport;
    @Resource
    private SearchService elasticSearchService;

    /**
     * 视频投稿
     */
    @PostMapping("/videos")
    public R<String> addVideos(@RequestBody Video video) {
        Long userId = userSupport.getCurrentUserId();
        video.setUserId(userId);
        video.setCreateTime(LocalDateTime.now());
        video.setUpdateTime(LocalDateTime.now());

        videoService.addVideos(video);
//        elasticSearchService.addVideo(video); // async
        return R.ok(video.getId().toString());
    }

    /**
     * 分页查询视频列表
     */
    @GetMapping("/videos")
    public R<PageResult<Video>> pageListVideos(
            Long size,
            Long no,
            @RequestParam(required = false) String partition) {
        PageResult<Video> result = videoService.pageListVideos(size, no, partition);
        return R.ok(result);
    }

    /**
     * 视频在线播放
     */
    @GetMapping("/video-slices")
    public R<Void> viewVideoOnlineBySlices(
            HttpServletRequest request,
            HttpServletResponse response,
            String bucket,
            String objectName,
            Long size
    ) {
        videoService.viewVideoOnlineBySlices(request, response, bucket, objectName, size);
        return R.ok();
    }

    /**
     * 点赞视频
     */
    @PostMapping("/video-likes")
    public R<Void> addVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();  // token 方法内抛出错误
        videoService.addVideoLike(userId, videoId);
        return R.ok();
    }

    /**
     * 取消点赞视频
     */
    @DeleteMapping("/video-likes")
    public R<Void> deleteVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return R.ok();
    }

    /**
     * 查询视频点赞数量
     */
    @GetMapping("/video-likes")
    public R<Map<String, Object>> getVideoLikes(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoLikes(videoId, UserHolder.getUserId());
        return R.ok(result);
    }


    /**
     * 收藏视频
     */
    @PostMapping("/video-collections")
    public R<Void> addVideoCollection(@RequestBody VideoCollection videoCollection) {
        videoService.addVideoCollection(videoCollection, UserHolder.getUserId());
        return R.ok();
    }

    /**
     * 取消收藏视频
     */
    @DeleteMapping("/video-collections")
    public R<Void> deleteVideoCollection(@RequestParam Long videoId) {
        videoService.deleteVideoCollection(videoId, UserHolder.getUserId());
        return R.ok();
    }

    /**
     * 查询视频收藏数量
     */
    @GetMapping("/video-collections")
    public R<Map<String, Object>> getVideoCollections(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoCollections(videoId, UserHolder.getUserId());
        return R.ok(result);
    }

    /**
     * 视频投币
     */
    @PostMapping("/video-coins")
    public R<Void> addVideoCoins(@RequestBody VideoCoin videoCoin) {
        videoService.addVideoCoins(videoCoin, UserHolder.getUserId());
        return R.ok();
    }

    /**
     * 查询视频投币数量
     */
    @GetMapping("/video-coins")
    public R<Map<String, Object>> getVideoCoins(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoCoins(videoId, UserHolder.getUserId());
        return R.ok(result);
    }

    /**
     * 添加视频评论
     */
    @PostMapping("/video-comments")
    public R<Void> addVideoComment(@RequestBody VideoComment videoComment) {
        videoComment.setUserId(UserHolder.getUserId());
        videoService.addVideoComment(videoComment);
        return R.ok();
    }

    /**
     * 分页查询视频评论
     */
    @GetMapping("/video-comments")
    public R<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer size,
                                                             @RequestParam Integer no,
                                                             @RequestParam Long videoId) {
        PageResult<VideoComment> result = videoService.pageListVideoComments(size, no, videoId);
        return R.ok(result);
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/video-details")
    public R<Map<String, Object>> getVideoDetails(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoDetails(videoId);
        return R.ok(result);
    }

    /**
     * 添加视频观看记录
     */
    @PostMapping("/video-views")
    public R<Void> addVideoView(@RequestBody VideoView videoView,
                                HttpServletRequest request) {
        videoView.setUserId(UserHolder.getUserId());
        videoService.addVideoView(videoView, request);
        return R.ok();
    }

    /**
     * 查询视频播放量
     */
    @GetMapping("/video-view-counts")
    public R<Integer> getVideoViewCounts(@RequestParam Long videoId) {
        Integer count = videoService.getVideoViewCounts(videoId);
        return R.ok(count);
    }

    /**
     * 视频内容推荐
     */
    @GetMapping("/recommendation/byUsers")
    public R<List<Video>> recommendByUsers(Integer count) {
        List<Video> list = videoService.recommendByUser(UserHolder.getUserId(), count);
        return R.ok(list);
    }

    /**
     * 视频内容推荐
     */
    @GetMapping("/recommendation/byContents")
    public R<List<Video>> recommendByContents(Long videoId) {
        List<Video> list = videoService.recommendByItem(UserHolder.getUserId()
                , videoId, 5);
        return R.ok(list);
    }

    /**
     * 视频帧截取生成黑白剪影
     */
    @GetMapping("/video-frames")
    public R<List<VideoBinaryPicture>> captureVideoFrame(@RequestParam Long videoId) {
        List<VideoBinaryPicture> list = videoService.convertVideoToImage(videoId);
        return R.ok(list);
    }

    /**
     * 查询视频黑白剪影
     */
    @GetMapping("/video-binary-images")
    public R<List<VideoBinaryPicture>> getVideoBinaryImages(@RequestParam Long videoId,
                                                            Long videoTimestamp,
                                                            String frameNo) {
        Map<String, Object> params = new HashMap<>();
        params.put("videoId", videoId);
        params.put("videoTimestamp", videoTimestamp);
        params.put("frameNo", frameNo);
        List<VideoBinaryPicture> list = videoService.getVideoBinaryImages(params);
        return R.ok(list);
    }

    /**
     * 查询视频标签
     */
    @GetMapping("/video-tags")
    public R<List<VideoTag>> getVideoTagsByVideoId(@RequestParam Long videoId) {
        List<VideoTag> list = videoService.getVideoTagsByVideoId(videoId);
        return R.ok(list);
    }

    /**
     * 删除视频标签
     */
    @DeleteMapping("/video-tags")
    public R<Void> deleteVideoTags(@RequestBody JSONObject params) {
        String tagIdList = params.getString("tagIdList");
        Long videoId = params.getLong("videoId");
        videoService.deleteVideoTags(JSON.parseArray(tagIdList).toJavaList(Long.class), videoId);
        return R.ok();
    }
}
