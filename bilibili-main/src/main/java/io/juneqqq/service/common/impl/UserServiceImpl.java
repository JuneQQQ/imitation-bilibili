package io.juneqqq.service.common.impl;


import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.juneqqq.cache.UserInfoCacheManager;
import io.juneqqq.dao.mapper.UserMapper;
import io.juneqqq.dao.mapper.UserInfoMapper;
import io.juneqqq.constant.UserConstant;
import io.juneqqq.core.entity.PageResult;
import io.juneqqq.dao.entity.User;
import io.juneqqq.dao.entity.UserInfo;
import io.juneqqq.core.exception.CustomException;
import io.juneqqq.pojo.dto.cache.CacheUserInfoDto;
import io.juneqqq.service.common.SearchService;
import io.juneqqq.service.common.UserAuthService;
import io.juneqqq.service.common.UserService;
import io.juneqqq.util.MD5Util;
import io.juneqqq.util.RSAUtil;
import io.juneqqq.util.UserSupport;
import com.mysql.cj.util.StringUtils;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl implements UserService {
    @Resource
    private UserSupport userSupport;

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
    private SearchService elasticSearchService;

    public UserInfo getUserInfo(Long userId) {
        UserInfo res = new UserInfo();  // 将这个对象当做 vo
        CacheUserInfoDto dto = userInfoCacheManager.getUserInfo(userId);
        BeanUtil.copyProperties(dto, res);
        return res;
    }

    @Transactional
    public void addUser(User user) {
        String phone = user.getPhone();
        if (StringUtils.isNullOrEmpty(phone)) {
            throw new CustomException("手机号不能为空！");
        }
        User dbUser = this.getUserByPhone(phone);
        if (dbUser != null) {
            throw new CustomException("该手机号已经注册！");
        }
        String salt = String.valueOf(System.currentTimeMillis());
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new CustomException("密码解密失败！");
        }
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
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
        userInfo.setCreateTime(LocalDateTime.now());
        userInfo.setUpdateTime(LocalDateTime.now());
        userInfoMapper.insert(userInfo);

        //添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
        //同步用户信息数据到es
        elasticSearchService.addUserInfo(userInfo);
    }


    public void updateCoin(Long userId, Integer target) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setCoin(target);
        userInfoMapper.update(userInfo, new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId));
    }

    public User getUserByPhone(String phone) {
        return userMapper.getUserByPhone(phone);
    }

    @SneakyThrows
    public String login(User user) {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new CustomException("参数异常！");
        }
        User dbUser = userMapper.getUserByPhoneOrEmail(phone, email);
        if (dbUser == null) {
            throw new CustomException("当前用户不存在！");
        }
        String password = user.getPassword();
        String rawPassword;
        try {
            // 因为传过来的密码是RSA非对称加密的，这里先解密得到原始密码
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new CustomException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        // 数据库里的密码是md5加密过的
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new CustomException("密码错误！");
        }
        return userSupport.generateToken(dbUser.getId());
    }

    public User getUser(Long userId) {
        User user = userMapper.getUserById(userId);
        UserInfo userInfo = userMapper.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUsers(User user) {
        Long id = user.getId();
        // 为了查询密码盐值，所以要再查一遍数据库
        User dbUser = userMapper.getUserById(id);
        if (dbUser == null) {
            throw new CustomException("用户不存在！");
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            // 解密前端传来的密码
            String rawPassword = null;
            try {
                rawPassword = RSAUtil.decrypt(user.getPassword());
            } catch (Exception e) {
                throw new CustomException("密码解密失败");
            }
            // 加密原始密码
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        userMapper.updateById(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(LocalDateTime.now());
        userMapper.updateUserInfos(userInfo);
    }

    public User getUserById(Long followingId) {
        return userMapper.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoMapper.selectList(new LambdaQueryWrapper<UserInfo>().
                in(UserInfo::getUserId, userIdList));
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        LambdaQueryWrapper<UserInfo> wrapper =
                new LambdaQueryWrapper<UserInfo>()
                        .orderByDesc(UserInfo::getUserId);

        Optional.ofNullable(params.get("nick")).ifPresent(n ->
                wrapper.like(UserInfo::getNick, params.get("nick")));
        // mybatis-plus 会自动查询 count(*) 记录数
        Page<UserInfo> userInfoPage = userInfoMapper.selectPage(new Page<>(
                        (Long) params.get("no"),
                        (Long) params.get("size")
                ),
                wrapper
        );
        return PageResult.of(
                userInfoPage.getTotal(),
                userInfoPage.getCurrent(),
                userInfoPage.getSize(),
                userInfoPage.getRecords()
        );
    }

    public String loginForDts(User user) {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new CustomException("参数异常！");
        }
        User dbUser = userMapper.getUserByPhoneOrEmail(phone, email);
        if (dbUser == null) {
            throw new CustomException("当前用户不存在！");
        }
        String password = user.getPassword();
        String rawPassword;
        try {
            rawPassword = RSAUtil.decrypt(password);
        } catch (Exception e) {
            throw new CustomException("密码解密失败！");
        }
        String salt = dbUser.getSalt();
        String md5Password = MD5Util.sign(rawPassword, salt, "UTF-8");
        if (!md5Password.equals(dbUser.getPassword())) {
            throw new CustomException("密码错误！");
        }
        Long userId = dbUser.getId();

        String accessToken = null;
        String refreshToken;
        try {
            accessToken = userSupport.generateToken(userId);
            refreshToken = userSupport.generateRefreshToken(userId);
        } catch (Exception e) {
            throw new CustomException(e.getMessage());
        }
        stringRedisTemplate.opsForValue().set(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId + ":",
                refreshToken, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return accessToken;
    }

    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoMapper.selectList(new LambdaQueryWrapper<>(UserInfo.class)
                .in(UserInfo::getUserId, userIdList));
//        return userDao.batchGetUserInfoByUserIds(userIdList);
    }

    public String getRefreshTokenByUserId(Long userId) {
        return userMapper.getRefreshTokenByUserId(userId);
    }

    public Integer getCoinAmount(Long userId) {
        UserInfo userInfo = userInfoMapper.selectOne(new LambdaQueryWrapper<UserInfo>().select(UserInfo::getCoin).eq(UserInfo::getUserId, userId));
        return userInfo.getCoin();
    }

}
