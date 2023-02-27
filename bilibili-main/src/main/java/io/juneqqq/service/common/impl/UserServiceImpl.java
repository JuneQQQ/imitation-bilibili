package io.juneqqq.service.common.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.juneqqq.cache.UserInfoCacheManager;
import io.juneqqq.cache.CacheConstant;
import io.juneqqq.core.auth.auth.SystemConfigConstant;
import io.juneqqq.core.exception.BusinessException;
import io.juneqqq.core.exception.ErrorCodeEnum;
import io.juneqqq.pojo.dao.mapper.UserMapper;
import io.juneqqq.pojo.dao.mapper.UserInfoMapper;
import io.juneqqq.constant.UserConstant;
import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.pojo.dao.entity.User;
import io.juneqqq.pojo.dao.entity.UserInfo;
import io.juneqqq.pojo.dao.repository.UserInfoDtoRepository;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
import io.juneqqq.pojo.dto.request.LoginUserDtoReq;
import io.juneqqq.pojo.dto.response.LoginUserDtoResp;
import io.juneqqq.service.common.UserAuthService;
import io.juneqqq.service.common.UserFollowingService;
import io.juneqqq.service.common.UserService;
import io.juneqqq.util.JwtUtils;
import io.juneqqq.util.MD5Util;
import io.juneqqq.util.RSAUtil;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserMapper userMapper;
    @Resource
    private UserInfoMapper userInfoMapper;
    @Resource
    private UserAuthService userAuthService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private UserInfoCacheManager userInfoCacheManager;
    @Resource
    private UserInfoDtoRepository userInfoDtoRepository;
    @Resource
    private UserFollowingService userFollowingService;

    @Resource
    private JwtUtils jwtUtils;
    @Value("${system.jwt.expire-seconds}")
    private Integer expire;

    public UserInfo getUserInfo(Long userId) {
        UserInfo res = new UserInfo();  // 将这个对象当做 vo
        CacheUserInfoDto dto = userInfoCacheManager.getUserInfo(userId);
        BeanUtil.copyProperties(dto, res);
        return res;
    }

    @Override
    public PageResult<UserInfo> pageListUserInfos(Long current, Long size, String nick, Long userId) {
        LambdaQueryWrapper<UserInfo> wrapper =
                new LambdaQueryWrapper<UserInfo>()
                        .orderByDesc(UserInfo::getUserId);
        Optional.ofNullable(nick).ifPresent(n ->
                wrapper.like(UserInfo::getNick, nick));
        // mybatis-plus 会自动查询 count(*) 记录数
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(new Page<>(current, size), wrapper);
        return PageResult.of(
                userInfoPage.getTotal(),
                userInfoPage.getCurrent(),
                userInfoPage.getSize(),
                userInfoPage.getRecords()
        );
    }

    @Override
    public List<EsUserInfoDto> selectBatchEsUserInfoDto(int current, int size) {
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(new Page<>(current, size), null);
        List<EsUserInfoDto> list = new ArrayList<>();
        for (UserInfo userInfo : userInfoPage.getRecords()) {
            EsUserInfoDto euid = new EsUserInfoDto();
            BeanUtil.copyProperties(userInfo, euid);
            // 设置粉丝数
            euid.setFanCount(userFollowingService.getUserFanCount(userInfo.getUserId()));
            // 设置 level
            euid.setLevel(1);  // TODO
            // 设置 isVip
            euid.setIsVip(false); // TODO
            list.add(euid);
        }
        return list;
    }

    @Override
    public EsUserInfoDto getEsUserInfoDto(Long userId) {
        EsUserInfoDto euid = new EsUserInfoDto();
        UserInfo userInfo = getUserInfo(userId);
        BeanUtil.copyProperties(userInfo, euid);
        euid.setFanCount(userFollowingService.getUserFanCount(userInfo.getUserId()));
        // 设置 level
        euid.setLevel(1);  // TODO
        // 设置 isVip
        euid.setIsVip(false); // TODO
        return euid;
    }

    @Override
    public void logout(Long userId,String token) {
//        stringRedisTemplate.delete(CacheConstant.USER_REFRESH_TOKEN_CACHE_NAME + ":" + token);
        userInfoCacheManager.evictUserInfoCacheByUserId(userId);
        // do nothing else
    }

    @Transactional
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new BusinessException(ErrorCodeEnum.USER_REGISTER_PHONE_IS_NULL);
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new BusinessException(ErrorCodeEnum.USER_REGISTER_PHONE_HAS_EXISTS);
        }
        String salt = String.valueOf(System.currentTimeMillis());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.USER_REGISTER_PASSWORD_DECODE_EXCEPTION);
        }
        String md5Password = MD5Util.sign(rawPassword, salt, StandardCharsets.UTF_8.toString());
        user.setSalt(salt);
        user.setPassword(md5Password);
        userMapper.addUser(user);

        //添加用户信息
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(user.getId());
        userInfo.setNick(UserConstant.DEFAULT_NICK);
        userInfo.setBirth(UserConstant.DEFAULT_BIRTH);
        userInfo.setGender(UserConstant.GENDER_MALE);
        userInfo.setAvatar(UserConstant.DEFAULT_AVATAR);
        userInfo.setSign(UserConstant.DEFAULT_SIGN);
        userInfo.setCoin(0);
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoMapper.insert(userInfo);
        //添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
        //同步用户信息数据到es
        EsUserInfoDto dto = new EsUserInfoDto();
        BeanUtil.copyProperties(userInfo, dto);
        dto.setIsVip(false);
        dto.setFanCount(0);
        dto.setLevel(UserConstant.DEFAULT_LEVEL);
        userInfoDtoRepository.save(dto);
    }


    /**
     * 级联缓存分析：
     * 本地/Redis：涉及coin，需要更新
     * elasticsearch：涉及coin，需要更新
     */
    @Transactional
    public void updateCoin(Long userId, Integer target) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setCoin(target);
        userInfoMapper.update(userInfo, new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId));
        // update cache
        userInfoCacheManager.evictUserInfoCacheByUserId(userId);
        // update es
        // get -> remove -> insert
        userInfoCacheManager.evictUserInfoCacheByUserId(userId);
        EsUserInfoDto esUserInfoDto = userInfoDtoRepository.searchByUserId(userId);
        userInfoDtoRepository.deleteByUserId(userId);
        esUserInfoDto.setCoin(target);
        userInfoDtoRepository.save(esUserInfoDto);
    }

    public User getUserByPhone(String phone) {
        return userMapper.getUserByPhone(phone);
    }

    @SneakyThrows
    public LoginUserDtoResp login(LoginUserDtoReq user) {
        String username = user.getUsername();
        User dbUser = userMapper.selectOne(new LambdaQueryWrapper<>(User.class)
                .eq(User::getPhone, username)
                .or()
                .eq(User::getEmail, username));

        // 简单处理
        dbUser = Optional.ofNullable(dbUser).orElseThrow(() -> new BusinessException(ErrorCodeEnum.USER_ACCOUNT_NOT_EXIST));

        String inputPasswd;
        try {
            // 因为传过来的密码是RSA非对称加密的，这里先解密得到原始密码
            inputPasswd = RSAUtil.decrypt(user.getPassword());
        } catch (Exception e) {
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);
        }

        String md5Password = MD5Util.sign(inputPasswd, dbUser.getSalt(), StandardCharsets.UTF_8.toString());
        if (!md5Password.equals(dbUser.getPassword()))
            throw new BusinessException(ErrorCodeEnum.USER_PASSWORD_ERROR);

        UserInfo userInfo = getUserInfo(dbUser.getId());

        String accessToken = jwtUtils.generateToken(userInfo.getUserId(), SystemConfigConstant.BILIBILI_FRONT_KEY);
        // redis 刷新标识  - ttl 为 token过期时间+5min，保证用户在过期时间附近不会重新登陆
        stringRedisTemplate.opsForValue().set(
                CacheConstant.USER_REFRESH_TOKEN + ":" + accessToken,
                String.valueOf(dbUser.getId()), (long) expire + 60 * 5, TimeUnit.SECONDS);

        return new LoginUserDtoResp(
                accessToken,
                userInfo.getNick(),
                String.valueOf(dbUser.getId())
        );
    }

    public User getUser(Long userId) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<>(User.class)
                .eq(User::getId, userId));
        user.setPassword(null);
        user.setSalt(null);
        CacheUserInfoDto cacheUserInfoDto = userInfoCacheManager.getUserInfo(userId);
        UserInfo userInfo = new UserInfo();
        BeanUtil.copyProperties(cacheUserInfoDto, userInfo);
        user.setUserInfo(userInfo);
        return user;
    }

    /**
     * 级联缓存分析：
     * User表没有任何存在于缓存的字段，故不需要更新任何缓存
     */
    public void updateUsers(User user) {
        Long id = user.getId();
        // 为了查询密码盐值，所以要再查一遍数据库
        User dbUser = userMapper.getUserById(id);
        if (dbUser == null) {
            throw new BusinessException(ErrorCodeEnum.TARGET_USER_NOT_EXISTS);
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            // 解密前端传来的密码
            String rawPassword = null;
            try {
                rawPassword = RSAUtil.decrypt(user.getPassword());
            } catch (Exception e) {
                throw new BusinessException(ErrorCodeEnum.USER_REGISTER_PASSWORD_DECODE_EXCEPTION);
            }
            // 加密原始密码
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), StandardCharsets.UTF_8.toString());
            user.setPassword(md5Password);
        }
        userMapper.updateById(user);
    }

    /**
     * 级联缓存分析：
     * 本地/Redis：涉及很多，需要更新
     * elasticsearch：涉及很多，需要更新
     */
    public void updateUserInfo(UserInfo userInfo) {
        userInfo.setUpdateTime(LocalDateTime.now());
        userMapper.updateUserInfos(userInfo);

        // update cache
        userInfoCacheManager.evictUserInfoCacheByUserId(userInfo.getUserId());
        // update es
        // get -> remove -> insert
        userInfoCacheManager.evictUserInfoCacheByUserId(userInfo.getUserId());
        EsUserInfoDto esUserInfoDto = userInfoDtoRepository.searchByUserId(userInfo.getUserId());
        // userInfo 参数的很多字段可能为null，这里用不为 null 的字段覆盖一下 es 中的数据
        BeanUtil.copyProperties(esUserInfoDto, userInfo, CopyOptions.create().setIgnoreNullValue(true));
        userInfoDtoRepository.deleteByUserId(userInfo.getUserId());
        userInfoDtoRepository.save(esUserInfoDto);
    }

    public User getUserById(Long followingId) {
        return userMapper.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>().
                in(UserInfo::getUserId, userIdList));
    }


    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoMapper.selectList(new LambdaQueryWrapper<>(UserInfo.class)
                .in(UserInfo::getUserId, userIdList));
    }

    public Integer getCoinAmount(Long userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().select(UserInfo::getCoin).eq(UserInfo::getUserId, userId));
        return userInfo.getCoin();
    }

}
