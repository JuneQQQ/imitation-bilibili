package io.juneqqq.service.common.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.juneqqq.pojo.dao.entity.*;
import io.juneqqq.pojo.dao.mapper.*;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dao.repository.esmodel.EsVideoDto;
import io.juneqqq.pojo.dto.UserPreference;
import io.juneqqq.pojo.dto.database.VideoLCC;
import io.juneqqq.pojo.dto.response.FileUploadResponse;
import io.juneqqq.service.common.FileService;
import io.juneqqq.service.common.VideoService;
import io.juneqqq.util.ImageUtil;
import io.juneqqq.util.IpUtil;
import io.juneqqq.util.MD5Util;
import eu.bitwalker.useragentutils.UserAgent;
import io.minio.GetObjectResponse;
import io.netty.util.internal.StringUtil;
import jakarta.servlet.ServletOutputStream;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.GenericPreference;
import org.apache.mahout.cf.taste.impl.model.GenericUserPreferenceArray;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.RandomRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import javax.imageio.ImageIO;

import jakarta.servlet.http.HttpServletRequest;

import jakarta.servlet.http.HttpServletResponse;

import java.awt.image.BufferedImage;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static java.io.File.createTempFile;

@Service
@Slf4j
public class VideoServiceImpl implements VideoService {

    @Resource
    private VideoMapper videoMapper;
    @Resource
    private UserServiceImpl userService;
    @Resource
    private ImageUtil imageUtil;

    @Resource(name = "MinioFileServiceImpl")
    private FileService fileService;

    @Resource
    private VideoLikeMapper videoLikeMapper;
    @Resource
    private VideoCollectionMapper videoCollectionMapper;
    @Resource
    private VideoCoinMapper videoCoinMapper;
    @Resource
    VideoCommentMapper videoCommentMapper;

    private static final int FRAME_NO = 30;

    @Override
    public List<EsVideoDto> selectBatchEsVideoDto(int current, int size) {
        Page<Video> videoPage = videoMapper.selectPage(new Page<>(current, size), null);
        List<EsVideoDto> list = new ArrayList<>();
        for (Video video : videoPage.getRecords()) {
            EsVideoDto evd = new EsVideoDto();
            BeanUtil.copyProperties(video, evd);
            evd.setNick(userService.getUserInfo(video.getUserId()).getNick());
            VideoLCC videoLCC = getVideoLCC(video.getId());
            BeanUtil.copyProperties(videoLCC, evd);
            list.add(evd);
        }
        return list;
    }

    @Transactional
    public void addVideos(Video video) {
        videoMapper.insert(video);
        Long videoId = video.getId();
        List<VideoTag> tagList = video.getVideoTagList();
        Optional.ofNullable(video.getVideoTagList()).ifPresent(list -> {
            list.forEach(l -> l.setVideoId(videoId));
            videoMapper.batchAddVideoTags(tagList);
        });
    }

    public PageResult<Video> pageListVideos(Long size, Long current, String partition) {

        LambdaQueryWrapper<Video> wrapper = new LambdaQueryWrapper<>();
        if (partition != null) wrapper.eq(Video::getPartition, partition);

        Page<Video> videoPage = videoMapper.selectPage(new Page<>(current, size), wrapper);

        return PageResult.of(
                videoPage.getTotal(),
                videoPage.getCurrent(),
                videoPage.getSize(),
                videoPage.getRecords()
        );
    }

    @SuppressWarnings("All")
    public void viewVideoOnlineBySlices(HttpServletRequest request,
                                        HttpServletResponse response,
                                        String bucket, String objectName, Long size) {
        Enumeration<String> headerNames = request.getHeaderNames();
        Map<String, Object> headers = new HashMap<>();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.put(header, request.getHeader(header));
        }
        String rangeStr = request.getHeader("Range");
        log.debug("原始Range：" + rangeStr);
        String[] range;
        if (StringUtil.isNullOrEmpty(rangeStr)) {
            rangeStr = "bytes=0-" + (size - 1);
        }
        range = rangeStr.split("bytes=|-");
        long start = 0;
        long end = size - 1;  // = size-1
        if (range.length == 2) {
            // 形如 bytes=15000-  [,1500]
            // 不能全给他！在申请的基础上延伸4M
            start = Long.parseLong(range[1]);
            end = Math.min(start + 4 * 1024 * 1024, end);
        } else if (range.length == 3) {
            // 形如 bytes=1500-3000 [,1500,3000]
            start = Long.parseLong(range[1]);
            end = Long.parseLong(range[2]);
        }

        long len = (end - start) + 1;
        String contentRange = "bytes " + start + "-" + end + "/" + size;
        log.debug("\n修改后的Content-Range:" + contentRange);
        response.setHeader("Content-Range", contentRange);
        response.setContentLength((int) len);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("Content-Type", "octet/stream");
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        // A timeout exceeded while waiting to proceed with the request, please reduce your request rate
        try (GetObjectResponse fis = fileService.getFileInputStream(
                objectName,
                bucket,
                start,
                len,
                null)) {
            byte[] buf = new byte[1024 * 16]; // 16kb
            double total = 0;
            try (ServletOutputStream os = response.getOutputStream()) {
                while ((len = fis.read(buf)) != -1) {
                    os.write(buf, 0, (int) len);
                    total += len;
                }
                log.debug("total:" + total / 1024 / 1024 + "M");
            } catch (IOException e) {
                log.error("\n连接异常：" + e.getMessage());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new BusinessException(ErrorCodeEnum.FILE_UPLOAD_ERROR);
        }
    }

    public void addVideoLike(Long userId, Long videoId) {
        VideoLike videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);

        Video video = videoMapper.selectById(videoLike.getVideoId());
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);
        Long before = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoLike.getVideoId()).eq(VideoLike::getUserId, videoLike.getUserId()));
        if (before > 0) throw new BusinessException(ErrorCodeEnum.VIDEO_HAS_BEEN_LIKE_BY_SAME_USER);
        videoLikeMapper.insert(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoLikeMapper.delete(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId)
                .eq(VideoLike::getUserId, userId));
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoLikeMapper.selectCount(new LambdaQueryWrapper<VideoLike>().eq(VideoLike::getVideoId, videoId));
        VideoLike videoLike = videoLikeMapper.selectOne(new LambdaQueryWrapper<VideoLike>()
                .eq(VideoLike::getVideoId, videoId).eq(VideoLike::getUserId, userId));
        Map<String, Object> result = new HashMap<>();
        result.put("count", count == null ? 0 : count);
        result.put("like", videoLike != null);
        return result;
    }


    @Transactional
    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if (videoId == null || groupId == null) {
            throw new BusinessException(ErrorCodeEnum.USER_ERROR);
        }
        Video video = videoMapper.selectById(videoId);
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);
        //删除原有视频收藏
        videoMapper.deleteVideoCollection(videoId, userId);
        //添加新的视频收藏
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(LocalDateTime.now());
        videoCollectionMapper.insert(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoCollectionMapper.delete(new LambdaQueryWrapper<VideoCollection>()
                .eq(VideoCollection::getVideoId, videoId).eq(VideoCollection::getUserId, userId));
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoCollectionMapper.selectCount(new LambdaQueryWrapper<VideoCollection>().eq(VideoCollection::getVideoId, videoId));
        VideoCollection videoCollection = videoCollectionMapper.selectOne(new LambdaQueryWrapper<VideoCollection>().eq(VideoCollection::getVideoId, videoId).eq(VideoCollection::getUserId, userId));
        boolean like = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }


    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        if (videoId == null) throw new BusinessException(ErrorCodeEnum.USER_ERROR);
        Integer amount = videoCoin.getAmount();
        if (amount > 3 || amount < 0) throw new BusinessException(ErrorCodeEnum.COIN_OUT_OF_LIMIT);
        Video video = videoMapper.getVideoById(videoId);
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);
        //查询当前登录用户是否拥有足够的硬币
        Integer userCoinsAmount = userService.getCoinAmount(userId);
        userCoinsAmount = userCoinsAmount == null ? 0 : userCoinsAmount;
        if (amount > userCoinsAmount) throw new BusinessException(ErrorCodeEnum.COIN_NOT_ENOUGH);
        // 查询已投
        VideoCoin dbVideoCoin = videoMapper.getVideoCoinByVideoIdAndUserId(videoId, userId);
        // 新增视频投币
        if (dbVideoCoin == null) {
            // 没有投过，那随便投
            videoCoin.setUserId(userId);
            videoCoinMapper.insert(videoCoin);
        } else {
            // 投过币了，想再投
            if (dbVideoCoin.getAmount() + amount > 3) throw new BusinessException(ErrorCodeEnum.COIN_OUT_OF_LIMIT);
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount += amount;
            //更新视频投币
            videoCoin.setId(dbVideoCoin.getId());
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            int update = videoCoinMapper.update(videoCoin, new LambdaQueryWrapper<VideoCoin>()
                    .eq(VideoCoin::getVideoId, videoId).eq(VideoCoin::getUserId, userId));
            if (update != 1) throw new BusinessException(ErrorCodeEnum.UNKNOW_EXCEPTION);
        }
        //更新用户当前硬币总数
        userService.updateCoin(userId, (userCoinsAmount - amount));
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count = videoMapper.getVideoCoinsAmount(videoId);
        VideoCoin videoCollection = videoMapper.getVideoCoinByVideoIdAndUserId(videoId, userId);
        Map<String, Object> result = new HashMap<>();
        result.put("count", count == null ? 0 : count);
        result.put("like", videoCollection != null);
        return result;
    }

    public void addVideoComment(VideoComment videoComment) {
        Long videoId = videoComment.getVideoId();
        if (videoId == null) throw new BusinessException(ErrorCodeEnum.USER_ERROR);

        Video video = videoMapper.getVideoById(videoId);
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);

        videoCommentMapper.insert(videoComment);
    }


    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoMapper.getVideoById(videoId);
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);

        LambdaQueryWrapper<VideoComment> wrapper = new LambdaQueryWrapper<>();
        if (videoId != null) wrapper.eq(VideoComment::getVideoId, videoId);
        wrapper.eq(VideoComment::getParentId, 0);  // parentId=0 表示这是一条根记录

        // 分页查根记录
        Page<VideoComment> page = videoCommentMapper.selectPage(new Page<>(no, size), wrapper);
        List<VideoComment> list;
        if (page.getTotal() > 0) {
            list = page.getRecords();
            //批量查询二级评论
            List<Long> parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment> childCommentList = videoCommentMapper.selectList(new LambdaQueryWrapper<>(VideoComment.class)
                    .in(VideoComment::getParentId, parentIdList));
            //批量查询用户信息
            Set<Long> userIdList = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdList = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> childUserIdList = childCommentList.stream().map(VideoComment::getReplyUserId).collect(Collectors.toSet());
            userIdList.addAll(replyUserIdList);
            userIdList.addAll(childUserIdList);
            // 查询回复与被回复者用户Info
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdList);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream()
                    .collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
            // 再区分
            list.forEach(comment -> {
                Long id = comment.getId();
                List<VideoComment> childList = new ArrayList<>();
                childCommentList.forEach(child -> {
                    if (id.equals(child.getParentId())) {
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setChildList(childList);
                comment.setUserInfo(userInfoMap.get(comment.getUserId()));
            });
        }

        return PageResult.of(
                page.getTotal(),
                page.getCurrent(),
                page.getSize(),
                page.getRecords()
        );
    }

    public Map<String, Object> getVideoDetails(Long videoId) {
        Video video = videoMapper.selectById(videoId);
        if (video == null) throw new BusinessException(ErrorCodeEnum.TARGET_VIDEO_NOT_EXISTS);
        FileInfo fileInfo = fileService.getFileInfoById(video.getFileId());
        Long userId = video.getUserId();
        User user = userService.getUser(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("videoInfo", video);
        result.put("userInfo", userInfo);
        result.put("fileInfo", fileInfo);
        return result;
    }

    /**
     * 添加播放记录
     */
    public void addVideoView(VideoView videoView, HttpServletRequest request) {
        Long userId = videoView.getUserId();
        Long videoId = videoView.getVideoId();
        //生成clientId
        String agent = request.getHeader("User-Agent");
        UserAgent userAgent = UserAgent.parseUserAgentString(agent);
        String clientId = String.valueOf(userAgent.getId());
        String ip = IpUtil.getIP(request);

        Map<String, Object> params = new HashMap<>();
        if (userId != null) {
            params.put("userId", userId);
        } else {
            params.put("ip", ip);
            params.put("clientId", clientId);
        }
        params.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        params.put("videoId", videoId);
        //添加观看记录
        VideoView dbVideoView = videoMapper.getVideoView(params);
        if (dbVideoView == null) {
            videoView.setIp(ip);
            videoView.setClientId(clientId);
            videoView.setCreateTime(LocalDateTime.now());
            videoView.setUpdateTime(LocalDateTime.now());
            videoMapper.addVideoView(videoView);
        }
    }

    /**
     * 查询播放量
     */
    public Integer getVideoViewCounts(Long videoId) {
        return videoMapper.getVideoViewCounts(videoId);
    }

    /**
     * 基于用户的协同推荐
     *
     * @param userId 用户id
     */
    @SneakyThrows
    public List<Video> recommendByUser(Long userId, Integer count) {
        List<UserPreference> list = videoMapper.getAllUserPreference();
        // 创建数据模型
        DataModel dataModel = this.createDataModel(list);
        // 获取用户相似程度
        UncenteredCosineSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        // 获取相似用户
        UserNeighborhood userNeighborhood = new NearestNUserNeighborhood(10, similarity, dataModel);
        // 构建推荐器
        Recommender recommender = new GenericUserBasedRecommender(dataModel, userNeighborhood, similarity);
        // 推荐视频
        List<RecommendedItem> recommendedItems = recommender.recommend(userId, count);
        List<Long> itemIds = recommendedItems.stream().map(RecommendedItem::getItemID).collect(Collectors.toList());
        if (CollectionUtil.isEmpty(itemIds)) {
            // 空的推荐？？？那么就随机推荐count个视频
            try {
                RandomRecommender randomRecommender = new RandomRecommender(dataModel);
                itemIds = randomRecommender.recommend(userId, count).stream().map(RecommendedItem::getItemID)
                        .distinct()
                        .collect(Collectors.toList());
            } catch (TasteException e) {
                throw new RuntimeException(e);
            }
        }
        return videoMapper.batchGetVideosByIds(itemIds);
    }


    /**
     * 基于内容的协同推荐
     *
     * @param userId  用户id
     * @param itemId  参考内容id（根据该内容进行相似内容推荐）
     * @param howMany 需要推荐的数量
     */
    @SneakyThrows
    public List<Video> recommendByItem(Long userId, Long itemId, int howMany) {
        List<UserPreference> list = videoMapper.getAllUserPreference();
        //创建数据模型
        DataModel dataModel = this.createDataModel(list);
        //获取内容相似程度
        ItemSimilarity similarity = new UncenteredCosineSimilarity(dataModel);
        GenericItemBasedRecommender genericItemBasedRecommender = new GenericItemBasedRecommender(dataModel, similarity);
        // 物品推荐相拟度，计算两个物品同时出现的次数，次数越多任务的相拟度越高
        List<Long> itemIds = genericItemBasedRecommender.recommendedBecause(userId, itemId, howMany)
                .stream()
                .map(RecommendedItem::getItemID)
                .collect(Collectors.toList());
        //推荐视频
        return videoMapper.batchGetVideosByIds(itemIds);
    }

    private DataModel createDataModel(List<UserPreference> userPreferenceList) {
        FastByIDMap<PreferenceArray> fastByIdMap = new FastByIDMap<>();
        Map<Long, List<UserPreference>> map = userPreferenceList.stream().collect(Collectors.groupingBy(UserPreference::getUserId));
        Collection<List<UserPreference>> list = map.values();
        for (List<UserPreference> userPreferences : list) {
            GenericPreference[] array = new GenericPreference[userPreferences.size()];
            for (int i = 0; i < userPreferences.size(); i++) {
                UserPreference userPreference = userPreferences.get(i);
                GenericPreference item = new GenericPreference(userPreference.getUserId(), userPreference.getVideoId(), userPreference.getValue());
                array[i] = item;
            }
            // array[0] userId 是登录用户的id
            fastByIdMap.put(array[0].getUserID(), new GenericUserPreferenceArray(Arrays.asList(array)));
        }
        return new GenericDataModel(fastByIdMap);
    }

    public Video getVideoById(Long videoId) {
        return videoMapper.selectById(videoId);
    }

    private String getObjectName(FileInfo fileInfo) {
        return fileInfo.getHash() + "-" + fileInfo.getFileName();
    }

    /**
     * 此方法十分耗时，请选择合适的执行逻辑
     * 视频下载到本地 + 分帧，每一帧上传百度人像识别平台返回结果 + 存库
     */
    @SneakyThrows
    public List<VideoBinaryPicture> convertVideoToImage(Long videoId) {

        // 查询文件信息
        log.debug("查询文件信息中...");
        long l1 = System.currentTimeMillis();
        Video videoInfo = getVideoById(videoId);
        FileInfo fileInfo = fileService.getFileInfoById(videoInfo.getFileId());
        String suffix = FileUtil.getSuffix(fileInfo.getFileName());
        File file = File.createTempFile("slice-tmp", "." + suffix);
        long l2 = System.currentTimeMillis();
        log.debug("查询文件耗时：{}", l2 - l1);
        // 下载文件
        log.debug("开始下载文件...");
        long l3 = System.currentTimeMillis();
        fileService.download(fileInfo.getBucket(), getObjectName(fileInfo), file);
        long l4 = System.currentTimeMillis();
        log.debug("文件下载耗时：{}", l4 - l3);

        String filePath = file.getAbsolutePath();

        log.debug("开始分离视频人物...");
        FFmpegFrameGrabber fFmpegFrameGrabber = FFmpegFrameGrabber.createDefault(filePath);
        fFmpegFrameGrabber.start();
        int ffLength = fFmpegFrameGrabber.getLengthInFrames();
        Frame frame;
        Java2DFrameConverter converter = new Java2DFrameConverter();
        int count = 1;
        List<VideoBinaryPicture> pictures = new ArrayList<>();
        for (int i = 1; i <= ffLength; i++) {
            long timestamp = fFmpegFrameGrabber.getTimestamp();
            frame = fFmpegFrameGrabber.grabImage();
            if (count == i) {
                // 无效帧
                if (frame == null) throw new BusinessException(ErrorCodeEnum.SYSTEM_ERROR);
                BufferedImage bufferedImage = converter.getBufferedImage(frame);
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "png", os);
                InputStream inputStream = new ByteArrayInputStream(os.toByteArray());
                //输出黑白剪影文件
                BufferedImage binaryImg = imageUtil.getBodyOutline(bufferedImage, inputStream);
                File outputFile = createTempFile("convert-" + videoId + "-", ".png");
                ImageIO.write(binaryImg, "png", outputFile);
                //有的浏览器或网站需要把图片白色的部分转为透明色，使用以下方法可实现
                imageUtil.transferAlpha(outputFile, outputFile);

                //上传视频剪影文件
                FileUploadResponse image = fileService.upload(outputFile, "image", MD5Util.hashFile(outputFile));

                VideoBinaryPicture videoBinaryPicture = new VideoBinaryPicture();
                videoBinaryPicture.setFrameNo(i);
                videoBinaryPicture.setUrl(image.getUrl());
                videoBinaryPicture.setVideoId(videoId);
                videoBinaryPicture.setVideoTimestamp(timestamp);
                videoBinaryPicture.setCreateTime(LocalDateTime.now());
                videoBinaryPicture.setUpdateTime(LocalDateTime.now());
                pictures.add(videoBinaryPicture);

                count += FRAME_NO;
                //删除临时文件
                outputFile.delete();
            }
        }
        //删除临时文件
        boolean delete = file.delete();
        if (!delete) log.warn("删除失败？？？");
        //批量添加视频剪影文件
        videoMapper.batchAddVideoBinaryPictures(pictures);
        log.debug("人物图片分离完毕，耗时：{}", System.currentTimeMillis() - l4);
        return pictures;
    }

    public List<VideoTag> getVideoTagsByVideoId(Long videoId) {
        return videoMapper.getVideoTagsByVideoId(videoId);
    }

    public void deleteVideoTags(List<Long> tagIdList, Long videoId) {
        videoMapper.deleteVideoTags(tagIdList, videoId);
    }

    public List<VideoBinaryPicture> getVideoBinaryImages(Map<String, Object> params) {
        return videoMapper.getVideoBinaryImages(params);
    }

    /**
     * 获取视频点赞、投币、收藏
     *
     * @param videoId 视频id
     * @return VideoLCC
     */
    public VideoLCC getVideoLCC(Long videoId) {
        VideoLCC videoLCC = new VideoLCC();
        CompletableFuture<Void> v1 = CompletableFuture.runAsync(() -> {
            videoLCC.setLike(videoMapper.getVideoLikes(videoId));
        });
        CompletableFuture<Void> v2 = CompletableFuture.runAsync(() -> {
            videoLCC.setCoin(videoMapper.getVideoCoins(videoId));
        });
        CompletableFuture<Void> v3 = CompletableFuture.runAsync(() -> {
            videoLCC.setCollection(videoMapper.getVideoCollections(videoId));
        });
        try {
            CompletableFuture.allOf(v1, v2, v3).get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("错误信息：" + e.getMessage());
            throw new BusinessException(ErrorCodeEnum.SYSTEM_TIMEOUT_ERROR);
        }
        return videoLCC;
    }
}
