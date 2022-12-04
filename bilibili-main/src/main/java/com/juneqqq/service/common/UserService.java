package com.juneqqq.service.common;


import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.juneqqq.dao.UserDao;
import com.juneqqq.dao.UserInfoDao;
import com.juneqqq.entity.constant.UserConstant;
import com.juneqqq.entity.dao.PageResult;
import com.juneqqq.entity.dao.User;
import com.juneqqq.entity.dao.UserInfo;
import com.juneqqq.entity.exception.CustomException;
import com.juneqqq.util.MD5Util;
import com.juneqqq.util.RSAUtil;
import com.juneqqq.util.UserSupport;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserService {
    @Resource
    private UserSupport userSupport;

    @Resource
    private UserDao userDao;
    @Resource
    private UserInfoDao userInfoDao;

    @Resource
    private UserAuthService userAuthService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ElasticSearchService elasticSearchService;

    public void addUserTest(User user) {
        userDao.insert(user);
    }


    public UserInfo getUserInfo(Long userId){
        return userInfoDao.selectOne(new LambdaQueryWrapper<>(UserInfo.class)
                .eq(UserInfo::getUserId,userId));
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
        userDao.addUser(user);

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
        userInfoDao.insert(userInfo);

        //添加用户默认权限角色
        userAuthService.addUserDefaultRole(user.getId());
        //同步用户信息数据到es
        elasticSearchService.addUserInfo(userInfo);
    }


    public void updateCoin(Long userId, Integer target) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(userId);
        userInfo.setCoin(target);
        userInfoDao.update(userInfo, new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getUserId, userId));
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public String login(User user) throws Exception {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new CustomException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
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
        User user = userDao.getUserById(userId);
        UserInfo userInfo = userDao.getUserInfoByUserId(userId);
        user.setUserInfo(userInfo);
        return user;
    }

    public void updateUsers(User user) throws Exception {
        Long id = user.getId();
        // 为了查询密码盐值，所以要再查一遍数据库
        User dbUser = userDao.getUserById(id);
        if (dbUser == null) {
            throw new CustomException("用户不存在！");
        }
        if (!StringUtils.isNullOrEmpty(user.getPassword())) {
            // 解密前端传来的密码
            String rawPassword = RSAUtil.decrypt(user.getPassword());
            // 加密原始密码
            String md5Password = MD5Util.sign(rawPassword, dbUser.getSalt(), "UTF-8");
            user.setPassword(md5Password);
        }
        userDao.updateById(user);
    }

    public void updateUserInfos(UserInfo userInfo) {
        userInfo.setUpdateTime(LocalDateTime.now());
        userDao.updateUserInfos(userInfo);
    }

    public User getUserById(Long followingId) {
        return userDao.getUserById(followingId);
    }

    public List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoDao.selectList(new LambdaQueryWrapper<UserInfo>().
                in(UserInfo::getUserId, userIdList));
    }

    public PageResult<UserInfo> pageListUserInfos(JSONObject params) {
        LambdaQueryWrapper<UserInfo> wrapper =
                new LambdaQueryWrapper<UserInfo>()
                        .orderByDesc(UserInfo::getUserId);

        Optional.ofNullable(params.get("nick")).ifPresent(n ->
                wrapper.like(UserInfo::getNick, params.get("nick")));
        // mybatis-plus 会自动查询 count(*) 记录数
        Page<UserInfo> userInfoPage = userInfoDao.selectPage(new Page<>(
                        (Long) params.get("no"),
                        (Long) params.get("size")
                ),
                wrapper
        );
        return new PageResult<>(userInfoPage.getTotal(), userInfoPage.getRecords());
    }

    public String loginForDts(User user) throws Exception {
        String phone = user.getPhone() == null ? "" : user.getPhone();
        String email = user.getEmail() == null ? "" : user.getEmail();
        if (StringUtils.isNullOrEmpty(phone) && StringUtils.isNullOrEmpty(email)) {
            throw new CustomException("参数异常！");
        }
        User dbUser = userDao.getUserByPhoneOrEmail(phone, email);
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

        String accessToken = userSupport.generateToken(userId);
        String refreshToken = userSupport.generateRefreshToken(userId);

        stringRedisTemplate.opsForValue().set(UserConstant.USER_REFRESH_TOKEN_PREFIX + userId + ":",
                refreshToken, UserConstant.USER_REFRESH_TOKEN_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        return accessToken;
    }

    public List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList) {
        return userInfoDao.selectList(new LambdaQueryWrapper<>(UserInfo.class)
                .in(UserInfo::getUserId, userIdList));
//        return userDao.batchGetUserInfoByUserIds(userIdList);
    }

    public String getRefreshTokenByUserId(Long userId) {
        return userDao.getRefreshTokenByUserId(userId);
    }

    public Integer getCoinAmount(Long userId) {
        UserInfo userInfo = userInfoDao.selectOne(new LambdaQueryWrapper<UserInfo>().select(UserInfo::getCoin).eq(UserInfo::getUserId, userId));
        return userInfo.getCoin();
    }
}
