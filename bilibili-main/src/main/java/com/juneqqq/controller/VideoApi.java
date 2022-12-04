package com.juneqqq.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.juneqqq.entity.dao.*;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.service.common.ElasticSearchService;
import com.juneqqq.service.common.VideoService;

import com.juneqqq.util.UserSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
public class VideoApi {

    @Resource
    private VideoService videoService;

    @Resource
    private UserSupport userSupport;
    @Resource
    private ElasticSearchService elasticSearchService;


    @GetMapping("es-videos")
    public R<List<Map<String, Object>>> searchVideo(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "1") String no,
            @RequestParam(required = false, defaultValue = "10") String size
    ) {
        List<Map<String, Object>> contents = elasticSearchService.getContents(keyword, Integer.parseInt(no), Integer.parseInt(size));
        return new R<>(contents);
    }

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
        //在es中添加一条视频数据
        elasticSearchService.addVideo(video);
        return new R<>(video.getId().toString());
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
        return new R<>(result);
    }

    /**
     * 视频在线播放
     */
    @GetMapping("/video-slices")
    public void viewVideoOnlineBySlices(
            HttpServletRequest request,
            HttpServletResponse response,
            String bucket,
            String objectName,
            Long size
    ) {
        videoService.viewVideoOnlineBySlices(request, response, bucket, objectName, size);
    }

    /**
     * 点赞视频
     */
    @PostMapping("/video-likes")
    public R<String> addVideoLike(@RequestBody VideoLike videoLike) {
        Long userId = userSupport.getCurrentUserId();
        videoLike.setUserId(userId);
        videoService.addVideoLike(videoLike);
        return R.success();
    }

    /**
     * 取消点赞视频
     */
    @DeleteMapping("/video-likes")
    public R<String> deleteVideoLike(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoLike(videoId, userId);
        return R.success();
    }

    /**
     * 查询视频点赞数量
     */
    @GetMapping("/video-likes")
    public R<Map<String, Object>> getVideoLikes(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (CustomException ignored) {
        }
        Map<String, Object> result = videoService.getVideoLikes(videoId, userId);
        return new R<>(result);
    }


    /**
     * 收藏视频
     */
    @PostMapping("/video-collections")
    public R<String> addVideoCollection(@RequestBody VideoCollection videoCollection) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCollection(videoCollection, userId);
        return R.success();
    }

    /**
     * 取消收藏视频
     */
    @DeleteMapping("/video-collections")
    public R<String> deleteVideoCollection(@RequestParam Long videoId) {
        Long userId = userSupport.getCurrentUserId();
        videoService.deleteVideoCollection(videoId, userId);
        return R.success();
    }

    /**
     * 查询视频收藏数量
     */
    @GetMapping("/video-collections")
    public R<Map<String, Object>> getVideoCollections(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception ignored) {
        }
        Map<String, Object> result = videoService.getVideoCollections(videoId, userId);
        return new R<>(result);
    }

    /**
     * 视频投币
     */
    @PostMapping("/video-coins")
    public R<String> addVideoCoins(@RequestBody VideoCoin videoCoin) {
        Long userId = userSupport.getCurrentUserId();
        videoService.addVideoCoins(videoCoin, userId);
        return R.success();
    }

    /**
     * 查询视频投币数量
     */
    @GetMapping("/video-coins")
    public R<Map<String, Object>> getVideoCoins(@RequestParam Long videoId) {
        Long userId = null;
        try {
            userId = userSupport.getCurrentUserId();
        } catch (Exception ignored) {
        }
        Map<String, Object> result = videoService.getVideoCoins(videoId, userId);
        return new R<>(result);
    }

    /**
     * 添加视频评论
     */
    @PostMapping("/video-comments")
    public R<String> addVideoComment(@RequestBody VideoComment videoComment) {
        Long userId = userSupport.getCurrentUserId();
        videoComment.setUserId(userId);
        videoService.addVideoComment(videoComment);
        return R.success();
    }

    /**
     * 分页查询视频评论
     */
    @GetMapping("/video-comments")
    public R<PageResult<VideoComment>> pageListVideoComments(@RequestParam Integer size,
                                                             @RequestParam Integer no,
                                                             @RequestParam Long videoId) {
        PageResult<VideoComment> result = videoService.pageListVideoComments(size, no, videoId);
        return new R<>(result);
    }

    /**
     * 获取视频详情
     */
    @GetMapping("/video-details")
    public R<Map<String, Object>> getVideoDetails(@RequestParam Long videoId) {
        Map<String, Object> result = videoService.getVideoDetails(videoId);
        return new R<>(result);
    }

    /**
     * 添加视频观看记录
     */
    @PostMapping("/video-views")
    public R<String> addVideoView(@RequestBody VideoView videoView,
                                  HttpServletRequest request) {
        Long userId;
        try {
            userId = userSupport.getCurrentUserId();  // userId
            videoView.setUserId(userId);
            videoService.addVideoView(videoView, request);
        } catch (Exception e) {
            log.debug("userId为null？{}", e.getMessage());
            videoService.addVideoView(videoView, request);
        }
        return R.success();
    }

    /**
     * 查询视频播放量
     */
    @GetMapping("/video-view-counts")
    public R<Integer> getVideoViewCounts(@RequestParam Long videoId) {
        Integer count = videoService.getVideoViewCounts(videoId);
        return new R<>(count);
    }

    /**
     * 视频内容推荐
     */
    @GetMapping("/recommendations")
    public R<List<Video>> recommend(Integer count) throws TasteException {
        Long userId = userSupport.getCurrentUserId();
        List<Video> list = videoService.recommendByUser(userId, count);
        return new R<>(list);
    }

    /**
     * 视频内容推荐
     */
    @GetMapping("/recommendations2")
    public R<List<Video>> recommend2(Long videoId) throws TasteException {
        Long userId = userSupport.getCurrentUserId();
        List<Video> list = videoService.recommendByItem(userId, videoId, 5);
        return new R<>(list);
    }

    /**
     * 视频帧截取生成黑白剪影
     */
    @GetMapping("/video-frames")
    public R<List<VideoBinaryPicture>> captureVideoFrame(@RequestParam Long videoId) throws Exception {
        List<VideoBinaryPicture> list = videoService.convertVideoToImage(videoId);
        return new R<>(list);
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
        return new R<>(list);
    }

    /**
     * 查询视频标签
     */
    @GetMapping("/video-tags")
    public R<List<VideoTag>> getVideoTagsByVideoId(@RequestParam Long videoId) {
        List<VideoTag> list = videoService.getVideoTagsByVideoId(videoId);
        return new R<>(list);
    }

    /**
     * 删除视频标签
     */
    @DeleteMapping("/video-tags")
    public R<String> deleteVideoTags(@RequestBody JSONObject params) {
        String tagIdList = params.getString("tagIdList");
        Long videoId = params.getLong("videoId");
        videoService.deleteVideoTags(JSONArray.parseArray(tagIdList).toJavaList(Long.class), videoId);
        return R.success();
    }
}
