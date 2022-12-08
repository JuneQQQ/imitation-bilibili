package io.juneqqq.cache;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.juneqqq.constant.CacheConstant;
import io.juneqqq.dao.entity.UserInfo;
import io.juneqqq.dao.mapper.UserInfoMapper;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

/**
 * 作家信息 缓存管理类
 *
 * @author xiongxiaoyang
 * @date 2022/5/12
 */
@Component
@Slf4j
public class UserInfoCacheManager {
    @Resource
    UserInfoMapper userInfoMapper;

    /**
     * 查询作家信息，并放入缓存中
     * unless = "#result == null"  不缓存空值
     */
    @Cacheable(cacheManager = CacheConstant.CACHE_TYPE_REDIS,
            value = CacheConstant.USER_INFO_CACHE_NAME)
    public CacheUserInfoDto getUserInfo(Long userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<>(UserInfo.class)
                .eq(UserInfo::getUserId, userId));
        if (userInfo == null) return null;
        CacheUserInfoDto cacheUserInfoDto = new CacheUserInfoDto();
        BeanUtil.copyProperties(userInfo, cacheUserInfoDto);
        log.debug("本次查询取自数据库，内容：{}", cacheUserInfoDto);
        return cacheUserInfoDto;
    }

    /**
     * 清除用户信息的缓存
     */
    @CacheEvict(cacheManager = CacheConstant.CACHE_TYPE_REDIS,
            value = CacheConstant.USER_INFO_CACHE_NAME)
    public void evictAuthorCache() {
        // none
        log.debug("用户缓存信息已删除~");
    }

}
