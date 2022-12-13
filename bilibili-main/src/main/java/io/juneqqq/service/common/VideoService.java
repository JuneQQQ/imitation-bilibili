package io.juneqqq.service.common;

import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.dao.entity.*;
import io.juneqqq.dao.repository.esmodel.EsVideoDto;
import io.juneqqq.pojo.dto.database.VideoLCC;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.Map;

public interface VideoService {
    List<EsVideoDto> selectBatchEsVideoDto(int current,int size);
    void addVideos(Video video);
    void addVideoComment(VideoComment videoComment);
    PageResult<Video> pageListVideos(Long size, Long current, String partition);
    void viewVideoOnlineBySlices(HttpServletRequest request,
                                 HttpServletResponse response,
                                 String bucket, String objectName, Long size);
    void addVideoLike(Long userId, Long videoId);
    void deleteVideoLike(Long videoId, Long userId);

    Map<String, Object> getVideoLikes(Long videoId, Long userId);
    void addVideoCollection(VideoCollection videoCollection, Long userId);

    void deleteVideoCollection(Long videoId, Long userId);

    Map<String, Object> getVideoCollections(Long videoId, Long userId);

    void addVideoCoins(VideoCoin videoCoin, Long userId);

    Map<String, Object> getVideoCoins(Long videoId, Long userId);

    PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId);

    Map<String, Object> getVideoDetails(Long videoId);

    void addVideoView(VideoView videoView, HttpServletRequest request);

    Integer getVideoViewCounts(Long videoId);

    List<Video> recommendByUser(Long userId, Integer count);

    List<Video> recommendByItem(Long userId, Long itemId, int howMany);

    Video getVideoById(Long videoId);

    List<VideoBinaryPicture> convertVideoToImage(Long videoId);

    List<VideoTag> getVideoTagsByVideoId(Long videoId);

    void deleteVideoTags(List<Long> tagIdList, Long videoId);

    List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params);

    VideoLCC getVideoLCC(Long videoId);
}
