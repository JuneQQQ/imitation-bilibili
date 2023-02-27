package io.juneqqq.service.common;

import io.juneqqq.pojo.dto.PageResult;
import io.juneqqq.pojo.dao.entity.User;
import io.juneqqq.pojo.dao.entity.UserInfo;
import io.juneqqq.pojo.dao.repository.esmodel.EsUserInfoDto;
import io.juneqqq.pojo.dto.request.LoginUserDtoReq;
import io.juneqqq.pojo.dto.response.LoginUserDtoResp;

import java.util.List;
import java.util.Set;

public interface UserService {
    Integer getCoinAmount(Long userId);

    List<UserInfo> batchGetUserInfoByUserIds(Set<Long> userIdList);

    List<UserInfo> getUserInfoByUserIds(Set<Long> userIdList);

    User getUserById(Long followingId);

    void updateUserInfo(UserInfo userInfo);

    void updateUsers(User user);

    /**
     * 获取User
     * @param userId 用户Id
     * @return User
     */
    User getUser(Long userId);

    /**
     * 登录接口
     * @param user user dto
     * @return LoginUserDtoResp
     */
    LoginUserDtoResp login(LoginUserDtoReq user);

    User getUserByPhone(String phone);

    void updateCoin(Long userId, Integer target);


    /**
     * 退出登录
     * @param userId 用户id
     * @param token 需要销毁的 token
     */
    void logout(Long userId, String token);

    void addUser(User user);


    UserInfo getUserInfo(Long userId);

    /**
     * 根据昵称分页查询用户
     */
    PageResult<UserInfo> pageListUserInfos(Long no, Long size, String nick, Long userId);

    /**
     * 分页查询库中的所有数据，因为id是递增的，所有不存在并发安全问题
     *
     * @param current 页码
     * @param size    每页大小
     * @return es-dto list
     */
    List<EsUserInfoDto> selectBatchEsUserInfoDto(int current, int size);

    EsUserInfoDto getEsUserInfoDto(Long userId);

}
